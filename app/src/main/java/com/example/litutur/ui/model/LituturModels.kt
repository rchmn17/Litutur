package com.example.litutur.ui.model

import androidx.compose.ui.graphics.vector.ImageVector

enum class LituturScreen {
    SPLASH_WELCOME,
    LOGIN,
    PARENT_REGISTRATION,
    CHILD_PROFILE_SETUP,
    PARENT_MAIN,
    CHILD_MODE,
    POST_DETAIL,
    CREATE_POST
}

enum class ParentNavTab(val label: String) {
    HOME("Beranda"),
    MONITORING("Monitoring"),
    MISSIONS("Misi"),
    COMMUNITY("Komunitas"),
    SETTINGS("Pengaturan")
}

data class ChildProfileData(
    val id: String,
    val name: String,
    val age: Int,
    val avatarName: String,
    val statusLevel: String,
    val comprehensionScore: Int,
    val isMandiri: Boolean
)

data class CommunityPost(
    val id: String,
    val authorName: String,
    val authorTitle: String,
    val avatarRes: String,
    val childInfo: String,
    val timeAgo: String,
    val caption: String,
    val milestoneTitle: String? = null,
    val milestoneSubtitle: String? = null,
    val milestoneStats: String? = null,
    val bookTitle: String? = null,
    val bookTag: String? = null,
    val likesCount: Int,
    val commentsCount: Int,
    val isLiked: Boolean = false,
    val isVerified: Boolean = true
)

data class PostComment(
    val id: String,
    val authorName: String,
    val authorBadge: String? = null,
    val timeAgo: String,
    val commentText: String,
    val likesCount: Int,
    val replies: List<PostComment> = emptyList()
)

data class InteractiveMission(
    val id: String,
    val title: String,
    val category: String,
    val pointsReward: Int,
    val isCompleted: Boolean,
    val completedDay: String? = null
)
