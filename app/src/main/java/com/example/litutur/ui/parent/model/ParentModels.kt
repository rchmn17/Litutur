package com.example.litutur.ui.parent.model

import androidx.compose.ui.graphics.vector.ImageVector

data class ChildProfile(
    val id: String,
    val name: String,
    val age: Int,
    val avatarRes: String = "avatar_1",
    val dailyLimitMinutes: Int = 60,
    val minutesUsedToday: Int = 35,
    val statusLevel: String = "Mandiri & Kritis"
)

data class ReadingStat(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: ImageVector
)

data class StoryActivity(
    val id: String,
    val storyTitle: String,
    val category: String,
    val durationMinutes: Int,
    val completedAt: String,
    val moralLesson: String,
    val quizScore: Int? = null
)

data class ParentMission(
    val id: String,
    val title: String,
    val description: String,
    val pointsReward: Int,
    val isCompleted: Boolean = false,
    val category: String = "Bonding Orang Tua"
)

enum class ParentTab(val label: String) {
    DASHBOARD("Beranda"),
    MONITORING("Monitoring"),
    MISSIONS("Misi"),
    SETTINGS("Pengaturan")
}
