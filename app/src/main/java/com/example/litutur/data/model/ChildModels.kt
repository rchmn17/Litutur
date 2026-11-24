package com.example.litutur.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Model Data untuk Buku Cerita Anak di Firestore ("books")
 */
data class CeritaBukuModel(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val category: String = "Fabel", // Fabel, Petualangan, Dongeng, Sains
    val coverUrl: String = "",
    val badgeText: String? = null, // "Favorit", "Baru", dsb.
    val level: String = "Level 1",
    val rating: Double = 4.9,
    val durationMinutes: Int = 5,
    val sinopsis: String = "",
    val moralCerita: String = "",
    val chapters: List<ChapterModel> = emptyList(),
    val quizQuestions: List<SoalKuisModel> = emptyList()
)

/**
 * Model Bab / Chapter dalam sebuah Buku
 */
data class ChapterModel(
    val chapterNumber: Int = 1,
    val title: String = "",
    val textContent: String = "",
    val illustrationUrl: String = "",
    val audioUrl: String? = null,
    val vocabWord: String? = null,
    val vocabMeaning: String? = null
)

/**
 * Model Soal Kuis Pemahaman
 */
data class SoalKuisModel(
    val questionId: String = "",
    val questionText: String = "",
    val illustrationUrl: String? = null,
    val options: List<OpsiKuisModel> = emptyList(),
    val correctAnswerId: String = "", // misal "B"
    val explanation: String = ""
)

/**
 * Model Opsi Jawaban Kuis
 */
data class OpsiKuisModel(
    val id: String = "",      // "A", "B", "C", "D"
    val label: String = "",   // "A"
    val optionText: String = ""
)

/**
 * Model Profil & Progres Gamifikasi Anak di Firestore ("child_profiles")
 */
data class ProfilAnakModel(
    @DocumentId
    val id: String = "",
    val parentId: String = "",
    val name: String = "Budi",
    val age: Int = 5,
    val avatarName: String = "singa_ceria",
    val totalXp: Int = 120,
    val totalCoins: Int = 50,
    val treeStage: Int = 2,            // 1: Tunas, 2: Bibit, 3: Pohon Muda, 4: Pohon Rimbun
    val treeProgress: Float = 0.75f,   // 0.0f sampai 1.0f untuk progress bar pohon
    val streakDays: Int = 3,
    val totalBooksFinished: Int = 0,
    // Peta skor minat baca (Kategori -> Bobot Bacaan)
    // Contoh: {"Fabel": 6, "Petualangan": 3, "Sains": 1}
    val interestScores: Map<String, Int> = emptyMap()
)

/**
 * Model Riwayat Bacaan Anak per Sesi / Chapter di Firestore ("reading_logs")
 */
data class RiwayatBacaModel(
    @DocumentId
    val id: String = "",
    val childId: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val category: String = "",
    val completedChapters: Int = 1,
    val totalChapters: Int = 1,
    val isFinished: Boolean = false,
    val durationSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Model Hasil Evaluasi Kuis Anak di Firestore ("quiz_results")
 */
data class HasilKuisModel(
    @DocumentId
    val id: String = "",
    val childId: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val scorePercentage: Int = 100, // 0 - 100
    val correctAnswers: Int = 3,
    val totalQuestions: Int = 3,
    val xpAwarded: Int = 50,
    val coinsAwarded: Int = 10,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Model Stiker dalam Album Stiker Gamifikasi Anak di Firestore ("stickers")
 */
data class StikerModel(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val category: String = "Sahabat Rimba", // Sahabat Rimba, Petualang Langit, Bintang Laut, Legendaris
    val imageUrl: String = "",
    val soundText: String = "",
    val levelText: String = "Lvl 1",
    val unlockCriteria: String = "",        // Syarat membuka stiker
    val isUnlocked: Boolean = false
)
