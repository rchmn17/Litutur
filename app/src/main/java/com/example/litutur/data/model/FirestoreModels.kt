package com.example.litutur.data.model

data class ParentUser(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val whatsapp: String = "",
    val avatarUrl: String = "",
    val parentPin: String = "1234",
    val createdAt: Long = System.currentTimeMillis()
)

data class ChildProfileEntity(
    val id: String = "",
    val name: String = "",
    val ageRange: String = "5-6 Thn",
    val age: Int = 6,
    val avatarName: String = "Kelinci Ceria 🐰",
    val avatarUrl: String = "",
    val favoriteTopics: List<String> = emptyList(),
    val interestScores: Map<String, Int> = emptyMap(),
    val dailyLimitMinutes: Int = 60,
    val minutesUsedToday: Int = 0,
    val points: Int = 0,
    val statusLevel: String = "Pemula Ceria",
    // Gamifikasi Mode Anak (disimpan di dokumen anak)
    val lastWateredDate: String = "",            // "yyyy-MM-dd" terakhir pohon disiram
    val lastMissionClaimDate: String = "",       // "yyyy-MM-dd" terakhir hadiah Misi Hari Ini diklaim
    val favoriteBookIds: List<String> = emptyList(),
    val purchasedStickerIds: List<String> = emptyList(), // stiker dari Kotak Stiker Misterius
    val coinsSpent: Int = 0,                     // koin terpakai; saldo koin = points - coinsSpent
    val createdAt: Long = System.currentTimeMillis()
)

data class ReadingLogEntity(
    val id: String = "",
    val storyId: String = "",
    val storyTitle: String = "",
    val category: String = "",
    val durationMinutes: Int = 0,
    val quizScore: Int = 0,
    val dayOfWeek: Int = 1, // 1 = Senin .. 7 = Minggu
    val moralLesson: String = "",
    val synopsis: String = "",
    val characters: List<String> = emptyList(),
    val quizQuestions: List<String> = emptyList(),
    val completedAt: Long = System.currentTimeMillis()
)

data class ParentMissionEntity(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "Bonding Orang Tua",
    val pointsReward: Int = 20,
    val isCompleted: Boolean = false,
    val completedDay: String? = null,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class CommunityPostEntity(
    val id: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val authorTitle: String = "Orang Tua Litutur",
    val authorAvatarUrl: String = "",
    val childInfo: String = "",
    val caption: String = "",
    val imageUrl: String = "",
    val milestoneTitle: String? = null,
    val milestoneSubtitle: String? = null,
    val milestoneStats: String? = null,
    val bookTitle: String? = null,
    val bookTag: String? = null,
    val likesCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val commentsCount: Int = 0,
    val isVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class PostCommentEntity(
    val id: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val authorBadge: String? = null,
    val commentText: String = "",
    val likesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
