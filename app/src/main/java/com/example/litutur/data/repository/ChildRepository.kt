package com.example.litutur.data.repository

import com.example.litutur.data.model.CeritaBukuModel
import com.example.litutur.data.model.ChapterModel
import com.example.litutur.data.model.ChildProfileEntity
import com.example.litutur.data.model.HasilKuisModel
import com.example.litutur.data.model.OpsiKuisModel
import com.example.litutur.data.model.ReadingLogEntity
import com.example.litutur.data.model.SoalKuisModel
import com.example.litutur.data.model.StikerModel
import android.util.Log
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository Backend untuk Fitur Anak:
 * 1. Manajemen data buku cerita & bank soal kuis (Firestore koleksi "books")
 * 2. Algoritma sorting buku berdasarkan minat baca anak (Flowchart 2.2.1)
 * 3. Algoritma gamifikasi pertumbuhan Pohon Kisah (Fungsi 3)
 * 4. Algoritma evaluasi pembukaan kunci Album Stiker (Fungsi 5)
 * 5. Sinkronisasi real-time profil & riwayat baca anak + penyimpanan hasil kuis
 */
class ChildRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val booksCol = firestore.collection("books")
    private val usersCol = firestore.collection("users")

    private fun childRef(uid: String, childId: String) =
        usersCol.document(uid).collection("children").document(childId)

    /**
     * Listener real-time dokumen profil anak (users/{uid}/children/{childId}).
     * Berkat latency compensation Firestore, perubahan poin dari kuis langsung terpancar
     * ke UI bahkan sebelum server mengonfirmasi (dan tetap jalan saat offline).
     */
    fun observeChildProfile(uid: String, childId: String): Flow<ChildProfileEntity?> = callbackFlow {
        val registration = childRef(uid, childId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "observeChildProfile gagal", error)
                return@addSnapshotListener // pertahankan nilai terakhir
            }
            val child = if (snapshot != null && snapshot.exists()) {
                snapshot.toObject(ChildProfileEntity::class.java)?.copy(id = snapshot.id)
            } else {
                null
            }
            trySend(child)
        }
        awaitClose { registration.remove() }
    }

    /**
     * Listener real-time riwayat baca anak, urut terbaru lebih dulu.
     * Dipakai untuk menghitung jumlah cerita selesai & skor kuis tertinggi (syarat stiker).
     */
    fun observeReadingLogs(uid: String, childId: String): Flow<List<ReadingLogEntity>> = callbackFlow {
        val registration = childRef(uid, childId)
            .collection("reading_logs")
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "observeReadingLogs gagal", error)
                    return@addSnapshotListener
                }
                val logs = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ReadingLogEntity::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(logs)
            }
        awaitClose { registration.remove() }
    }

    /**
     * Menyimpan hasil kuis anak secara atomik dalam satu WriteBatch:
     * 1. reading_logs/{id}  -> riwayat baca (dibaca Mode Orang Tua & syarat stiker)
     * 2. quiz_results/{id}  -> detail skor kuis
     * 3. dokumen anak       -> points += XP, minutesUsedToday += durasi,
     *                          interestScores[kategori] += 1 (bahan sorting minat)
     *
     * Catatan: commit() baru selesai setelah server mengonfirmasi. Saat offline perubahan
     * sudah tercatat di cache lokal dan listener sudah memancarkannya ke UI.
     */
    suspend fun saveQuizCompletion(
        uid: String,
        childId: String,
        log: ReadingLogEntity,
        result: HasilKuisModel,
        category: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val ref = childRef(uid, childId)
            val batch = firestore.batch()
            batch.set(ref.collection("reading_logs").document(log.id), log)
            batch.set(ref.collection("quiz_results").document(log.id), result)
            batch.update(
                ref,
                FieldPath.of("points"), FieldValue.increment(result.xpAwarded.toLong()),
                FieldPath.of("minutesUsedToday"), FieldValue.increment(log.durationMinutes.toLong()),
                // FieldPath.of aman untuk nama kategori yang mengandung spasi / '&' / '.'
                FieldPath.of("interestScores", category.ifBlank { "Lainnya" }), FieldValue.increment(1L)
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "saveQuizCompletion gagal", e)
            Result.failure(e)
        }
    }

    /**
     * Memperbarui sebagian field dokumen anak (siram pohon, klaim misi, favorit, kotak stiker).
     * Memakai update() biasa (bukan transaksi) agar tetap bekerja saat offline:
     * perubahan langsung masuk cache lokal & listener, lalu tersinkron saat online.
     */
    suspend fun updateChildFields(
        uid: String,
        childId: String,
        updates: Map<String, Any>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            childRef(uid, childId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "updateChildFields gagal: ${updates.keys}", e)
            Result.failure(e)
        }
    }

    /**
     * Mengambil seluruh buku dari Firestore.
     * Jika Firestore masih kosong, otomatis melakukan seeding data awal.
     */
    suspend fun getBooks(): Result<List<CeritaBukuModel>> = withContext(Dispatchers.IO) {
        val books = try {
            val snapshot = booksCol.get().await()
            if (snapshot.isEmpty) {
                // Seeder otomatis jika database baru/kosong
                seedDefaultBooks()
                getDefaultBooks()
            } else {
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(CeritaBukuModel::class.java)?.copy(id = doc.id)
                }.ifEmpty { getDefaultBooks() }
            }
        } catch (e: Exception) {
            // Fallback aman jika offline / kendala jaringan
            getDefaultBooks()
        }
        // Gambar yang sudah dipindahkan ke Cloudinary langsung memakai link Cloudinary
        Result.success(books.map { CloudinaryAssetSync.resolveBook(it) })
    }

    /**
     * Buku apa adanya dari SERVER Firestore (tanpa fallback & tanpa cache).
     * Dipakai sinkronisasi aset agar tidak pernah menimpa data asli dengan data default.
     */
    suspend fun getBooksFromServerRaw(): Result<List<CeritaBukuModel>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = booksCol.get(Source.SERVER).await()
            Result.success(
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(CeritaBukuModel::class.java)?.copy(id = doc.id)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** ID dokumen buku di Firestore (sama dengan yang dipakai seeder) */
    fun bookDocId(book: CeritaBukuModel): String =
        book.id.ifBlank { book.title.lowercase().replace(" ", "_").replace("&", "dan") }

    /**
     * Mengambil detail buku berdasarkan judul
     */
    suspend fun getBookByTitle(title: String): CeritaBukuModel = withContext(Dispatchers.IO) {
        val all = getBooks().getOrDefault(getDefaultBooks())
        all.find { it.title.equals(title, ignoreCase = true) }
            ?: getDefaultBooks().first()
    }

    /**
     * Problem Solving 1 (Flowchart 2.2.1):
     * Algoritma Sorting Rekomendasi Buku berdasarkan Minat Anak.
     * Buku dengan kategori yang paling sering dibaca anak diprioritaskan di urutan atas.
     */
    fun sortBooksByInterest(
        books: List<CeritaBukuModel>,
        interestScores: Map<String, Int>,
        favoriteTopics: List<String>
    ): List<CeritaBukuModel> {
        return books.sortedByDescending { book ->
            var weight = 0
            // Tambahkan bobot dari skor minat riwayat baca
            interestScores.forEach { (category, score) ->
                if (book.category.contains(category, ignoreCase = true) ||
                    category.contains(book.category, ignoreCase = true)) {
                    weight += score * 3
                }
            }
            // Tambahkan bobot jika termasuk topik favorit awal profil
            if (favoriteTopics.any { topic -> book.category.contains(topic, ignoreCase = true) }) {
                weight += 5
            }
            // Buku berating tinggi sebagai pertimbangan tambahan
            weight += (book.rating * 2).toInt()
            weight
        }
    }

    /**
     * Problem Solving 2 (Fungsi 3):
     * Algoritma Pertumbuhan Pohon Kisah berdasarkan Akumulasi Poin / XP Anak.
     * Menghasilkan Level Pohon (1-4), label tahap, dan persentase progress (0.0f - 1.0f).
     */
    fun calculateTreeGrowth(totalPoints: Int): TreeGrowthData {
        return when {
            totalPoints < 50 -> TreeGrowthData(
                stage = 1,
                stageLabel = "Tunas Kecil 🌱",
                progress = (totalPoints / 50f).coerceIn(0.1f, 1f),
                pointsToNext = 50 - totalPoints,
                nextStageLabel = "Bibit Hijau"
            )
            totalPoints < 120 -> TreeGrowthData(
                stage = 2,
                stageLabel = "Bibit Hijau 🌿",
                progress = ((totalPoints - 50) / 70f).coerceIn(0.1f, 1f),
                pointsToNext = 120 - totalPoints,
                nextStageLabel = "Pohon Muda"
            )
            totalPoints < 250 -> TreeGrowthData(
                stage = 3,
                stageLabel = "Pohon Muda 🌳",
                progress = ((totalPoints - 120) / 130f).coerceIn(0.1f, 1f),
                pointsToNext = 250 - totalPoints,
                nextStageLabel = "Pohon Rimbun Megah"
            )
            else -> TreeGrowthData(
                stage = 4,
                stageLabel = "Pohon Rimbun Megah 🍎✨",
                progress = 1.0f,
                pointsToNext = 0,
                nextStageLabel = "Level Maksimal"
            )
        }
    }

    /**
     * Problem Solving 3 (Fungsi 5):
     * Algoritma Evaluasi Pembukaan Kunci Album Stiker.
     * Mengevaluasi syarat gamifikasi: jumlah cerita selesai, streak baca, dan skor kuis.
     */
    fun evaluateStickersUnlock(
        storiesCount: Int,
        totalPoints: Int,
        maxQuizScore: Int,
        purchasedIds: Collection<String> = emptyList()
    ): List<StikerModel> =
        buildStickerList(storiesCount, totalPoints, maxQuizScore).map { sticker ->
            // Gambar dari Cloudinary (registry / koleksi "stickers") jika sudah tersedia
            val resolved = CloudinaryAssetSync.resolveSticker(sticker)
            // Stiker yang didapat dari Kotak Stiker Misterius selalu terbuka
            if (resolved.id in purchasedIds) resolved.copy(isUnlocked = true) else resolved
        }

    /** Katalog stiker dengan link gambar asli (dipakai sinkronisasi Cloudinary) */
    fun stickerCatalog(): List<StikerModel> = buildStickerList(0, 0, 0)

    private fun buildStickerList(
        storiesCount: Int,
        totalPoints: Int,
        maxQuizScore: Int
    ): List<StikerModel> {
        val baseList = listOf(
            StikerModel(
                id = "stiker_1",
                name = "Leo Singa",
                category = "Sahabat Rimba",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBJ5mHf8G0M15Voefy2MTmlGvGwhsjWmIgxFlACRwLiwvxJ98JNcpSTx1N5IeYqpOqhyzxyz9IEGkL1pVh_kiagmnxsuFSw2U1HihSruMoYTmjrucLyDHvcJnICmyytzF-acdjZ5Nu6K7AGo6uJMg8ZQnuE7YskaKQpPK8xhtgN-NJXmpW_0j0HacirOb6M6uQoiA4iUoju05I28Q3ZbbcbYoz9s3nUrwDtvgZGungc",
                soundText = "Leo Singa: Rawrr ceria! 🦁",
                levelText = "Lvl 1",
                unlockCriteria = "Stiker Awal Pendaftaran",
                isUnlocked = true
            ),
            StikerModel(
                id = "stiker_2",
                name = "Kelinci Ceria",
                category = "Sahabat Rimba",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDUUMx2TSYoKhZwSIEHjQb3jv6L4dw4M3kYHcEnxIk72iZ6i0shtbjj-l98JTyOByZ-mlynbhF7ym_yUgAHnE5o9jwFDRz-_uJcJxGZ-ZjlGF7ZIa8iQ6mmSItRdlA991UCvGlkL9VTewFUsKaICjVecKfVGBGGjd_xBcUxIIlygewk7fZMnPqcufYacBXkVtL5-iy1kuDGGlODE4DvLpV19flaVdozwOuOzSsGDdVM",
                soundText = "Kelinci Ceria: Hop hop senang! 🐰",
                levelText = "Baca 1 Cerita",
                unlockCriteria = "Selesaikan 1 cerita apapun",
                isUnlocked = storiesCount >= 1
            ),
            StikerModel(
                id = "stiker_3",
                name = "Kancil Cerdik",
                category = "Sahabat Rimba",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDUkxy0eP7Y7yN0DLskfzvocBBrwKgdZKEDhX1eTPFLoNXV7dhTc1OeEwZ0Rzx2QVaHkB-R2si5a3j2oMsoZ85lBBeX46_Eaw5qi4OU4XtyfiT_s7GwW8ZXzcNh_moIW78Icnarhkxo8VfWLrrYXqQZQ7KFmHqVJo4UGYMVv2KgRmkpNbuMWgkC6pOpDQ4PdEAWUUbYCxnO6fiQKcpDhdL3vmr6S0-qcb5gyda-eglH",
                soundText = "Kancil Cerdik: Siap berpetualang! 🦌",
                levelText = "Baca 2 Cerita",
                unlockCriteria = "Selesaikan 2 cerita dan kuis",
                isUnlocked = storiesCount >= 2
            ),
            StikerModel(
                id = "stiker_4",
                name = "Burung Hantu Bijak",
                category = "Petualang Langit",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAEBr3z4JawPmT-irh9R3xBCU3BRHCfzTOuhKl_U5tjm2w_korbCcEGuVhc_iXU23ufygJkj-6kVFxv99IYxvdWleio0dZugoA4k3rI73WLZNcelc-42YTOKzfHDxGfa7-gRngQUulkXM3-Vs8pn1r6zdPRdItIO8mrcj2O3H4hGhM4PHluO7ZJbRFTisCz1864cmgDQnlk0ETaehMzBub6euoGtJZlwhChxvG3-z_C",
                soundText = "Hantu Bijak: Hoot hoot pintar! 🦉",
                levelText = "Kuis Sempurna",
                unlockCriteria = "Dapatkan skor kuis 100%",
                isUnlocked = maxQuizScore >= 100 || storiesCount >= 3
            ),
            StikerModel(
                id = "stiker_5",
                name = "Gajah Ramah",
                category = "Sahabat Rimba",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCB5s0CW6951d4itUO9q0Y-aYXB2PZ8yivlJvhDHwGjKWsM_m5Rx_f56NOBuyNDzI6O6WME5gxmwgDBGsxCLxiJkw6EQAAWpaFvVqUcLa9gnPl6Nc6Wm6uNWuyGv-5gBFlHxA6HsAH3jOlCl_-JMYYEBQh5KMFGUimsA_CPPcSqFbW3pztA2U9C6I_gzDf-acUj5LQpaO4ue3IMpINwb0XOG6rrI8DhAg2OAQHA5J58",
                soundText = "Gajah: Teeeet semburan ceria! 🐘",
                levelText = "Poin 100+",
                unlockCriteria = "Kumpulkan lebih dari 100 poin",
                isUnlocked = totalPoints >= 100 || storiesCount >= 4
            ),
            StikerModel(
                id = "stiker_6",
                name = "Harimau Rimba",
                category = "Legendaris",
                imageUrl = "",
                soundText = "Harimau: Aummm hebat! 🐯",
                levelText = "Baca 5 Cerita",
                unlockCriteria = "Selesaikan 5 cerita berturut-turut",
                isUnlocked = storiesCount >= 5
            )
        )
        return baseList
    }

    /**
     * Seeding data buku cerita awal ke Firestore jika koleksi masih kosong
     */
    suspend fun seedDefaultBooks() = withContext(Dispatchers.IO) {
        try {
            val defaults = getDefaultBooks()
            defaults.forEach { book ->
                booksCol.document(bookDocId(book)).set(book).await()
            }
        } catch (_: Exception) {
            // Abaikan error seeder jika izin terbatas
        }
    }

    /**
     * Data master buku cerita anak (Lengkap dengan Chapters & Soal Kuis Interaktif)
     */
    fun getDefaultBooks(): List<CeritaBukuModel> {
        return listOf(
            CeritaBukuModel(
                id = "singa_baik",
                title = "Singa yang Baik Hati",
                category = "Fabel",
                coverUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAMnMXGf4IqB7Jm1Mq5egbAeEzKw_3XEnU30CfUV6WJFXOJegHqG1pCBDvzUrsQviuBFBXsVOtzjUpLtdleUkJQJSC6W4Yk570CAyj2iMiGbehUIkvINUVJk0buCcILXZ_fzTna8ujwJ0D0j_hjbVrMoq5mf7gopBNI3GhsnW9e_1BvxCGZmRCGn4HybVWI4MlaYGIyI9KmZ1vvOTA9kZHYQLOUK3GRNj2HdAaxDYoY",
                badgeText = "Favorit",
                level = "Level 2",
                rating = 4.9,
                durationMinutes = 8,
                sinopsis = "Kisah Leo sang raja rimba yang ramah dan selalu menolong hewan kecil di hutan belantara.",
                moralCerita = "Kebaikan hati dan kesediaan menolong orang lain akan selalu berbuah manis.",
                chapters = listOf(
                    ChapterModel(
                        chapterNumber = 1,
                        title = "Suara di Atas Pohon",
                        textContent = "Matahari pagi bersinar cerah di Hutan Rimba Hijau. Leo si Singa sedang berjalan-jalan di tepi sungai jernih. Tiba-tiba, ia mendengar suara cicitan pelan dari atas dahan pohon tinggi. 'Tolong... tolong...', suara itu terdengar cemas.",
                        illustrationUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB6FTTn9EFp03Fep4lQx-iorVyxZPV9mghlg2RxxulF34oiHI4Y0dJ_rwZmp5yHq3xJQeTvGfCVNYzt0w0MTlsEcBeqP5MVVhCBO-Wa-dXPY8YGy7m4nK6WUzM3zNz2Q7AfXrfWQ_U9btLw8W6o-dQePn4CqB4oXa5Oi69_2y_JO6fF3_hS5jyJFmIRv4xZwJvg-vwZrJILqTbZ7el8em5U0F87yKY0LGVqnbxxzK1S",
                        vocabWord = "Cemas",
                        vocabMeaning = "Merasa khawatir atau takut akan sesuatu hal"
                    ),
                    ChapterModel(
                        chapterNumber = 2,
                        title = "Menolong Burung Pipit",
                        textContent = "Leo mengangkat kepalanya yang megah. Ternyata seekor anak burung pipit kecil sayapnya tersangkut ranting kering. Leo melangkah perlahan agar tidak mengagetkannya. Dengan lembut dan hati-hati, Leo menjulurkan cakarnya untuk menggeser ranting tersebut sampai anak burung bisa terbang bebas!",
                        illustrationUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB6FTTn9EFp03Fep4lQx-iorVyxZPV9mghlg2RxxulF34oiHI4Y0dJ_rwZmp5yHq3xJQeTvGfCVNYzt0w0MTlsEcBeqP5MVVhCBO-Wa-dXPY8YGy7m4nK6WUzM3zNz2Q7AfXrfWQ_U9btLw8W6o-dQePn4CqB4oXa5Oi69_2y_JO6fF3_hS5jyJFmIRv4xZwJvg-vwZrJILqTbZ7el8em5U0F87yKY0LGVqnbxxzK1S",
                        vocabWord = "Megah",
                        vocabMeaning = "Tampak besar, indah, dan gagah"
                    )
                ),
                quizQuestions = listOf(
                    SoalKuisModel(
                        questionId = "q1",
                        questionText = "Apa yang dilakukan Leo ketika mendengar suara burung meminta tolong?",
                        options = listOf(
                            OpsiKuisModel("A", "A", "Mengabaikannya dan terus berjalan santai"),
                            OpsiKuisModel("B", "B", "Mendekati dahan dan menolongnya dengan hati-hati"),
                            OpsiKuisModel("C", "C", "Menyuruh hewan lain yang lebih kecil"),
                            OpsiKuisModel("D", "D", "Menertawakan anak burung tersebut")
                        ),
                        correctAnswerId = "B",
                        explanation = "Leo adalah singa yang baik hati, ia segera menolong anak burung dengan hati-hati."
                    ),
                    SoalKuisModel(
                        questionId = "q2",
                        questionText = "Sikap terpuji apa yang dapat kita teladani dari Leo si Singa?",
                        options = listOf(
                            OpsiKuisModel("A", "A", "Suka marah dan sombong karena kuat"),
                            OpsiKuisModel("B", "B", "Peduli dan suka menolong sesama makhluk hidup"),
                            OpsiKuisModel("C", "C", "Selalu ingin menang sendiri"),
                            OpsiKuisModel("D", "D", "Malas membantu teman yang kesusahan")
                        ),
                        correctAnswerId = "B",
                        explanation = "Leo mengajarkan kita untuk selalu peduli dan menolong siapa pun yang membutuhkan."
                    )
                )
            ),
            CeritaBukuModel(
                id = "kelinci_kurakura",
                title = "Kelinci & Kura-kura",
                category = "Fabel",
                coverUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAStevGosNmGTg0TQxsaYPJXmARrMwDYBIrrB_f94250v5rjLoEcd0DXH31qRrBIv6g3k1ER9ZuJ9BQW_LfnwTNLAG-Soau0YHDjb-wywx8u35cylLK5NfWuhXP6UoCgRhDU7eWrHKQ-0Mr0xs1F2AvEMVaVC-oKbjcI0skimC-gIcJvNheshm8exfB22aCyrCxR3TTScmZRcD7pcYhkKqDucX67vRGwEH563uK-qOQ",
                badgeText = "Baru",
                level = "Level 1",
                rating = 4.8,
                durationMinutes = 5,
                sinopsis = "Lomba lari legendaris yang mengajarkan arti ketekunan dan kerendahan hati.",
                moralCerita = "Ketekunan dan konsistensi akan selalu mengalahkan kesombongan.",
                chapters = listOf(
                    ChapterModel(
                        chapterNumber = 1,
                        title = "Tantangan di Garis Awal",
                        textContent = "Kelinci selalu membanggakan larinya yang gesit di hadapan semua penghuni padang rumput. Melihat Kura-kura berjalan lambat, Kelinci tertawa mengejek dan menantangnya berlomba lari hingga ujung bukit.",
                        illustrationUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAStevGosNmGTg0TQxsaYPJXmARrMwDYBIrrB_f94250v5rjLoEcd0DXH31qRrBIv6g3k1ER9ZuJ9BQW_LfnwTNLAG-Soau0YHDjb-wywx8u35cylLK5NfWuhXP6UoCgRhDU7eWrHKQ-0Mr0xs1F2AvEMVaVC-oKbjcI0skimC-gIcJvNheshm8exfB22aCyrCxR3TTScmZRcD7pcYhkKqDucX67vRGwEH563uK-qOQ",
                        vocabWord = "Gesit",
                        vocabMeaning = "Bergerak dengan sangat cepat dan lincah"
                    )
                ),
                quizQuestions = listOf(
                    SoalKuisModel(
                        questionId = "q1",
                        questionText = "Mengapa Kura-kura berhasil memenangkan perlombaan lari?",
                        options = listOf(
                            OpsiKuisModel("A", "A", "Kura-kura menggunakan sepatu roda ajaib"),
                            OpsiKuisModel("B", "B", "Kura-kura tekun melangkah tanpa berhenti sementara Kelinci tertidur"),
                            OpsiKuisModel("C", "C", "Kura-kura memotong jalan rahasia"),
                            OpsiKuisModel("D", "D", "Kelinci tersesat di dalam gua")
                        ),
                        correctAnswerId = "B",
                        explanation = "Kura-kura menang karena tekun dan tidak menyerah, sedangkan Kelinci meremehkan lawan."
                    )
                )
            ),
            CeritaBukuModel(
                id = "bintang_kejora",
                title = "Misteri Bintang Kejora",
                category = "Sains",
                coverUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCG-kdAJnekprDeOPX75fLVJ87g-yL76mMQQiqPzifUrSSm2RWFM8Q2PBL_-UfQ9Kfx5iZzu_JhCfY9Xpov-CQNKWPVCUItvOTqh_UUjdvWvaQA_eOMX1Xgi6_tM9qCu5Hun3jrlTUa10bI_QN9K1w_enTh87lDVNA62VMDqzLYM72SRm8LN43HdaHa-1WO0YfA5RYQsml6ZnPSL6N_hSoGU-3IuhtIeckyCgcPQIc1",
                level = "Level 2",
                rating = 5.0,
                durationMinutes = 10,
                sinopsis = "Petualangan seru mengamati langit malam dan mengenali benda-benda antariksa.",
                moralCerita = "Rasa ingin tahu dan belajar sains membuat wawasan kita luas.",
                chapters = listOf(
                    ChapterModel(
                        chapterNumber = 1,
                        title = "Cahaya Paling Terang",
                        textContent = "Malam itu langit cerah tanpa awan. Budi dan Ayah mengeluarkan teleskop kecil di halaman rumah. Ayah menunjuk sebuah cahaya terang yang tidak berkedip. 'Itu bukan bintang biasa, melainkan Planet Venus!', jelas Ayah dengan tersenyum.",
                        illustrationUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCG-kdAJnekprDeOPX75fLVJ87g-yL76mMQQiqPzifUrSSm2RWFM8Q2PBL_-UfQ9Kfx5iZzu_JhCfY9Xpov-CQNKWPVCUItvOTqh_UUjdvWvaQA_eOMX1Xgi6_tM9qCu5Hun3jrlTUa10bI_QN9K1w_enTh87lDVNA62VMDqzLYM72SRm8LN43HdaHa-1WO0YfA5RYQsml6ZnPSL6N_hSoGU-3IuhtIeckyCgcPQIc1",
                        vocabWord = "Teleskop",
                        vocabMeaning = "Alat teropong untuk melihat benda langit yang jauh agar tampak dekat dan jelas"
                    )
                ),
                quizQuestions = listOf(
                    SoalKuisModel(
                        questionId = "q1",
                        questionText = "Apa sebutan lain untuk Bintang Kejora menurut penjelasan Ayah?",
                        options = listOf(
                            OpsiKuisModel("A", "A", "Planet Mars"),
                            OpsiKuisModel("B", "B", "Planet Venus"),
                            OpsiKuisModel("C", "C", "Satelit Bulan"),
                            OpsiKuisModel("D", "D", "Komet Ekor Panjang")
                        ),
                        correctAnswerId = "B",
                        explanation = "Bintang Kejora yang bersinar terang di langit pagi/sore hari sebenarnya adalah Planet Venus."
                    )
                )
            ),
            CeritaBukuModel(
                id = "petualangan_kancil",
                title = "Petualangan Si Kancil",
                category = "Petualangan",
                coverUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCzVO_A2s58Q39eTvzKo-EIiCxyW0FTpp2R7COJUQMfo1t5hvgNtvq1ubSitCjq4tP7xbVy3Oi-cbq0B9xK8Y48YSbBbXf23FnDbl5fIQB0588aA6HsKxQjUKCRLi99KM0qAIuAK-9tfNYCW5iUMTLsX8qyNEwMNLWiD8NPkoT8zbDmuGRkfJeb83fI_n_Im5faCOW13KzdWMTcYM3buhBqefvAGWRa05-rLlG8alJq",
                level = "Level 3",
                rating = 4.7,
                durationMinutes = 7,
                sinopsis = "Kancil cerdik menyeberangi sungai deras penuh buaya dengan kecerdasannya.",
                moralCerita = "Gunakan kecerdasan akal untuk menyelesaikan rintangan hidup.",
                chapters = listOf(
                    ChapterModel(
                        chapterNumber = 1,
                        title = "Menghitung Buaya di Sungai",
                        textContent = "Kancil ingin sekali mencicipi mentimun ranum di seberang sungai. Namun sungai itu dipenuhi kawanan buaya. Dengan cerdik, Kancil mengajak buaya berbaris rapi dengan alasan ingin menghitung jumlah mereka atas perintah Raja Hutan.",
                        illustrationUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCzVO_A2s58Q39eTvzKo-EIiCxyW0FTpp2R7COJUQMfo1t5hvgNtvq1ubSitCjq4tP7xbVy3Oi-cbq0B9xK8Y48YSbBbXf23FnDbl5fIQB0588aA6HsKxQjUKCRLi99KM0qAIuAK-9tfNYCW5iUMTLsX8qyNEwMNLWiD8NPkoT8zbDmuGRkfJeb83fI_n_Im5faCOW13KzdWMTcYM3buhBqefvAGWRa05-rLlG8alJq",
                        vocabWord = "Ranum",
                        vocabMeaning = "Sudah masak atau matang benar (tentang buah)"
                    )
                ),
                quizQuestions = listOf(
                    SoalKuisModel(
                        questionId = "q1",
                        questionText = "Alasan apa yang dipakai Kancil agar buaya mau berbaris rapi di sungai?",
                        options = listOf(
                            OpsiKuisModel("A", "A", "Mengajak bermain petak umpet bersama"),
                            OpsiKuisModel("B", "B", "Ingin menghitung jumlah mereka atas perintah Raja Hutan"),
                            OpsiKuisModel("C", "C", "Menantang lomba berenang cepat"),
                            OpsiKuisModel("D", "D", "Memberikan hadiah ikan bakar")
                        ),
                        correctAnswerId = "B",
                        explanation = "Kancil berkata ingin menghitung jumlah buaya agar bisa melompati punggung mereka ke seberang."
                    )
                )
            )
        )
    }
}

private const val TAG = "ChildRepository"

/**
 * Data Model Hasil Kalkulasi Pohon Kisah Gamifikasi
 */
/**
 * Batas poin minimal tiap tahap Pohon Kisah (indeks 0 = tahap 1).
 * HARUS sama dengan ambang di [ChildRepository.calculateTreeGrowth]; dipakai layar untuk
 * menampilkan "xx+ Poin" pada daftar tahap.
 */
val TREE_STAGE_MIN_POINTS = listOf(0, 50, 120, 250)

data class TreeGrowthData(
    val stage: Int,             // 1: Tunas, 2: Bibit, 3: Pohon Muda, 4: Pohon Rimbun
    val stageLabel: String,
    val progress: Float,        // 0.0f - 1.0f
    val pointsToNext: Int,
    val nextStageLabel: String
)
