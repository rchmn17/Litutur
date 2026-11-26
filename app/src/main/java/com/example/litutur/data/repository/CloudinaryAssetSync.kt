package com.example.litutur.data.repository

import android.util.Log
import com.example.litutur.data.model.CeritaBukuModel
import com.example.litutur.data.model.StikerModel
import com.example.litutur.data.remote.CloudinaryUploadException
import com.example.litutur.data.remote.CloudinaryUploader
import com.example.litutur.data.remote.LituturAssets
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * Memindahkan seluruh ilustrasi (sampul buku, ilustrasi bab/kuis, ilustrasi layar) dan
 * gambar stiker ke Cloudinary, lalu mencatat link-nya di Firebase.
 *
 * Firestore:
 * - app_assets/{hash(sourceUrl)} -> { sourceUrl, cloudinaryUrl, publicId, folder, uploadedAt }
 *   (registry: setiap gambar hanya diunggah SEKALI untuk semua perangkat)
 * - books/{id}                   -> coverUrl & illustrationUrl ditulis ulang ke link Cloudinary
 * - stickers/{id}                -> katalog stiker + imageUrl Cloudinary
 *                                   (imageUrl di sini bisa diganti manual lewat Firebase Console)
 *
 * Tidak butuh akses dashboard Cloudinary: memakai unsigned upload preset.
 */
object CloudinaryAssetSync {

    private const val TAG = "CloudinaryAssetSync"

    data class AssetState(
        /** link sumber lama -> link Cloudinary */
        val bySource: Map<String, String> = emptyMap(),
        /** id stiker -> imageUrl dari koleksi "stickers" */
        val stickerImages: Map<String, String> = emptyMap()
    )

    data class SyncReport(
        val uploaded: Int,
        val alreadyInCloudinary: Int,
        val failed: List<String>,
        val booksUpdated: Int,
        val stickersSaved: Int
    )

    private val _state = MutableStateFlow(AssetState())
    val state: StateFlow<AssetState> = _state.asStateFlow()

    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val mutex = Mutex()
    private var registryLoaded = false
    private var syncedThisSession = false

    // ---------------------------------------------------------------------
    // Resolver: dipakai UI & repository untuk selalu menampilkan link terbaik
    // ---------------------------------------------------------------------

    fun resolve(url: String): String =
        if (url.isBlank()) url else _state.value.bySource[url] ?: url

    fun resolveSticker(sticker: StikerModel): StikerModel {
        val override = _state.value.stickerImages[sticker.id]?.takeIf { it.isNotBlank() }
        return sticker.copy(imageUrl = override ?: resolve(sticker.imageUrl))
    }

    fun resolveBook(book: CeritaBukuModel): CeritaBukuModel = book.copy(
        coverUrl = resolve(book.coverUrl),
        chapters = book.chapters.map { it.copy(illustrationUrl = resolve(it.illustrationUrl)) },
        quizQuestions = book.quizQuestions.map { q ->
            q.copy(illustrationUrl = q.illustrationUrl?.let { resolve(it) })
        }
    )

    // ---------------------------------------------------------------------
    // Registry
    // ---------------------------------------------------------------------

    /** Membaca registry app_assets & koleksi stickers (cepat, tanpa upload). */
    suspend fun loadRegistry(force: Boolean = false) = withContext(Dispatchers.IO) {
        if (registryLoaded && !force) return@withContext
        try {
            val assets = firestore.collection("app_assets").get().await()
            val bySource = assets.documents.mapNotNull { doc ->
                val source = doc.getString("sourceUrl")
                val cloud = doc.getString("cloudinaryUrl")
                if (source.isNullOrBlank() || cloud.isNullOrBlank()) null else source to cloud
            }.toMap()

            val stickers = firestore.collection("stickers").get().await()
            val stickerImages = stickers.documents.mapNotNull { doc ->
                doc.getString("imageUrl")?.takeIf { it.isNotBlank() }?.let { doc.id to it }
            }.toMap()

            _state.update { it.copy(bySource = it.bySource + bySource, stickerImages = it.stickerImages + stickerImages) }
            registryLoaded = true
        } catch (e: Exception) {
            Log.w(TAG, "Registry aset belum bisa dibaca", e)
        }
    }

    // ---------------------------------------------------------------------
    // Sinkronisasi (upload + tulis ulang link di Firestore)
    // ---------------------------------------------------------------------

    /**
     * Unggah semua gambar yang belum ada di Cloudinary, lalu perbarui Firestore.
     * Aman dipanggil berulang: gambar yang sudah tercatat di registry tidak diunggah lagi.
     * Butuh koneksi internet & pengguna sudah login (untuk izin tulis Firestore).
     */
    suspend fun syncAll(
        childRepository: ChildRepository = ChildRepository(),
        force: Boolean = false
    ): Result<SyncReport> = mutex.withLock {
        if (syncedThisSession && !force) {
            return@withLock Result.success(SyncReport(0, 0, emptyList(), 0, 0))
        }
        withContext(Dispatchers.IO) {
            try {
                loadRegistry(force = true)

                // 1) Data mentah langsung dari server (bukan cache), agar tidak menimpa data asli
                val rawBooks = childRepository.getBooksFromServerRaw().getOrThrow()
                val stickerCatalog = childRepository.stickerCatalog()

                // 2) Kumpulkan semua gambar yang perlu diunggah (unik per link sumber)
                val tasks = LinkedHashMap<String, Pair<String, String>>() // source -> (folder, publicId)
                var alreadyCloud = 0
                fun want(url: String?, folder: String, publicId: String) {
                    if (url.isNullOrBlank()) return
                    if (CloudinaryUploader.isCloudinaryUrl(url) || _state.value.bySource.containsKey(url)) {
                        alreadyCloud++
                        return
                    }
                    tasks.putIfAbsent(url, folder to publicId)
                }

                LituturAssets.uiAssets.forEach {
                    want(it.sourceUrl, LituturAssets.FOLDER_ILLUSTRATIONS, it.publicId)
                }
                stickerCatalog.forEach {
                    want(it.imageUrl, LituturAssets.FOLDER_STICKERS, "sticker_${slug(it.id)}")
                }
                // Database buku masih kosong -> tetap unggah ilustrasi buku bawaan
                val booksForAssets = rawBooks.ifEmpty { childRepository.getDefaultBooks() }
                booksForAssets.forEach { book ->
                    val bookSlug = slug(childRepository.bookDocId(book))
                    want(book.coverUrl, LituturAssets.FOLDER_ILLUSTRATIONS, "book_${bookSlug}_cover")
                    book.chapters.forEach { ch ->
                        want(ch.illustrationUrl, LituturAssets.FOLDER_ILLUSTRATIONS, "book_${bookSlug}_bab${ch.chapterNumber}")
                    }
                    book.quizQuestions.forEach { q ->
                        want(q.illustrationUrl, LituturAssets.FOLDER_ILLUSTRATIONS, "book_${bookSlug}_kuis_${slug(q.questionId)}")
                    }
                }

                // 3) Upload satu per satu & catat di registry
                var uploaded = 0
                val failed = mutableListOf<String>()
                for ((sourceUrl, target) in tasks) {
                    val (folder, publicId) = target
                    uploadOne(sourceUrl, folder, publicId).fold(
                        onSuccess = { cloudUrl ->
                            firestore.collection("app_assets").document(hash(sourceUrl)).set(
                                mapOf(
                                    "sourceUrl" to sourceUrl,
                                    "cloudinaryUrl" to cloudUrl,
                                    "publicId" to "$folder/$publicId",
                                    "folder" to folder,
                                    "uploadedAt" to System.currentTimeMillis()
                                )
                            ).await()
                            _state.update { it.copy(bySource = it.bySource + (sourceUrl to cloudUrl)) }
                            uploaded++
                        },
                        onFailure = { err ->
                            Log.w(TAG, "Gagal mengunggah $publicId", err)
                            failed += publicId
                        }
                    )
                }

                // 4) Tulis ulang link di koleksi books
                var booksUpdated = 0
                rawBooks.forEach { book ->
                    val resolved = resolveBook(book)
                    if (resolved != book) {
                        firestore.collection("books").document(childRepository.bookDocId(book))
                            .set(resolved).await()
                        booksUpdated++
                    }
                }

                // 5) Simpan katalog stiker + link Cloudinary ke koleksi stickers
                var stickersSaved = 0
                stickerCatalog.forEachIndexed { index, sticker ->
                    val current = _state.value.stickerImages[sticker.id]
                    val cloudUrl = resolve(sticker.imageUrl)
                    val needsWrite = current.isNullOrBlank() ||
                        (!CloudinaryUploader.isCloudinaryUrl(current) && CloudinaryUploader.isCloudinaryUrl(cloudUrl))
                    if (needsWrite) {
                        firestore.collection("stickers").document(sticker.id).set(
                            mapOf(
                                "name" to sticker.name,
                                "category" to sticker.category,
                                "imageUrl" to cloudUrl,
                                "soundText" to sticker.soundText,
                                "levelText" to sticker.levelText,
                                "unlockCriteria" to sticker.unlockCriteria,
                                "order" to index + 1
                            ),
                            SetOptions.merge()
                        ).await()
                        if (cloudUrl.isNotBlank()) {
                            _state.update { it.copy(stickerImages = it.stickerImages + (sticker.id to cloudUrl)) }
                        }
                        stickersSaved++
                    }
                }

                syncedThisSession = failed.isEmpty()
                val report = SyncReport(uploaded, alreadyCloud, failed, booksUpdated, stickersSaved)
                Log.i(TAG, "Sinkronisasi aset selesai: $report")
                Result.success(report)
            } catch (e: Exception) {
                Log.w(TAG, "Sinkronisasi aset dibatalkan", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Upload satu gambar: (1) Cloudinary mengambil langsung dari link,
     * (2) jika gagal, HP mengunduh lalu mengunggah bytes-nya.
     * Jika public_id sudah ada (diunggah perangkat lain), pakai link yang sudah ada.
     */
    private suspend fun uploadOne(sourceUrl: String, folder: String, publicId: String): Result<String> {
        val viaRemote = CloudinaryUploader.uploadFromUrl(sourceUrl, folder, publicId)
        if (viaRemote.isSuccess) return viaRemote
        if ((viaRemote.exceptionOrNull() as? CloudinaryUploadException)?.isAlreadyExists == true) {
            return Result.success(CloudinaryUploader.deliveryUrl(folder, publicId))
        }

        val viaDevice = CloudinaryUploader.downloadAndUpload(sourceUrl, folder, publicId)
        if (viaDevice.isSuccess) return viaDevice
        if ((viaDevice.exceptionOrNull() as? CloudinaryUploadException)?.isAlreadyExists == true) {
            return Result.success(CloudinaryUploader.deliveryUrl(folder, publicId))
        }
        return viaDevice
    }

    private fun slug(value: String): String =
        value.lowercase().replace(Regex("[^a-z0-9_-]+"), "_").trim('_').take(60).ifBlank { "aset" }

    private fun hash(value: String): String =
        MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(32)
}
