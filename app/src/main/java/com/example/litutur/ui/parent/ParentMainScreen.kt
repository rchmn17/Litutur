package com.example.litutur.ui.parent

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.litutur.data.model.CommunityPostEntity
import com.example.litutur.data.model.ParentMissionEntity
import com.example.litutur.ui.model.ParentNavTab
import com.example.litutur.ui.theme.CoralOrangePrimary
import com.example.litutur.ui.theme.LituturTheme
import com.example.litutur.ui.viewmodel.ParentUiState

@Composable
fun ParentMainScreen(
    uiState: ParentUiState = ParentUiState(),
    onSwitchToChildMode: () -> Unit = {},
    onSelectChild: (Int) -> Unit = {},
    onAddChildClick: () -> Unit = {},
    onRecordReading: (title: String, category: String, minutes: Int, quizScore: Int) -> Unit = { _, _, _, _ -> },
    onToggleMission: (ParentMissionEntity) -> Unit = {},
    onAddCustomMission: (title: String, description: String) -> Unit = { _, _ -> },
    onRefreshCommunity: () -> Unit = {},
    onNavigateToCreatePost: () -> Unit = {},
    onLikePost: (CommunityPostEntity) -> Unit = {},
    onOpenPostDetail: (CommunityPostEntity) -> Unit = {},
    onUpdatePin: (String) -> Unit = {},
    onUpdateDailyLimit: (Int) -> Unit = {},
    onClearMessages: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(ParentNavTab.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage, uiState.errorMessage) {
        val message = uiState.statusMessage ?: uiState.errorMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            onClearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Column {
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp
                ) {
                    val navColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CoralOrangePrimary,
                        selectedTextColor = CoralOrangePrimary,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = Color.White
                    )
                    NavigationBarItem(
                        selected = selectedTab == ParentNavTab.HOME,
                        onClick = { selectedTab = ParentNavTab.HOME },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                        label = { Text("Beranda", fontWeight = if (selectedTab == ParentNavTab.HOME) FontWeight.Bold else FontWeight.Medium) },
                        colors = navColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == ParentNavTab.MONITORING,
                        onClick = { selectedTab = ParentNavTab.MONITORING },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = "Monitoring") },
                        label = { Text("Monitoring", fontWeight = if (selectedTab == ParentNavTab.MONITORING) FontWeight.Bold else FontWeight.Medium) },
                        colors = navColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == ParentNavTab.MISSIONS,
                        onClick = { selectedTab = ParentNavTab.MISSIONS },
                        icon = { Icon(Icons.Default.TaskAlt, contentDescription = "Misi") },
                        label = { Text("Misi", fontWeight = if (selectedTab == ParentNavTab.MISSIONS) FontWeight.Bold else FontWeight.Medium) },
                        colors = navColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == ParentNavTab.COMMUNITY,
                        onClick = {
                            selectedTab = ParentNavTab.COMMUNITY
                            onRefreshCommunity()
                        },
                        icon = { Icon(Icons.Default.Forum, contentDescription = "Komunitas") },
                        label = { Text("Komunitas", fontWeight = if (selectedTab == ParentNavTab.COMMUNITY) FontWeight.Bold else FontWeight.Medium) },
                        colors = navColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == ParentNavTab.SETTINGS,
                        onClick = { selectedTab = ParentNavTab.SETTINGS },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Pengaturan") },
                        label = { Text("Pengaturan", fontWeight = if (selectedTab == ParentNavTab.SETTINGS) FontWeight.Bold else FontWeight.Medium) },
                        colors = navColors
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(160))
                },
                label = "ParentTabTransition"
            ) { tab ->
                when (tab) {
                    ParentNavTab.HOME -> ParentHomeScreen(
                        uiState = uiState,
                        onSwitchToChildMode = onSwitchToChildMode,
                        onSelectChild = onSelectChild,
                        onAddChildClick = onAddChildClick,
                        onOpenMissions = { selectedTab = ParentNavTab.MISSIONS },
                        onOpenMonitoring = { selectedTab = ParentNavTab.MONITORING }
                    )
                    ParentNavTab.MONITORING -> ParentAnalyticsScreen(
                        uiState = uiState,
                        onSelectChild = onSelectChild,
                        onRecordReading = onRecordReading
                    )
                    ParentNavTab.MISSIONS -> ParentMissionsScreen(
                        uiState = uiState,
                        onSelectChild = onSelectChild,
                        onToggleMission = onToggleMission,
                        onAddCustomMission = onAddCustomMission
                    )
                    ParentNavTab.COMMUNITY -> ParentCommunityScreen(
                        uiState = uiState,
                        onRefresh = onRefreshCommunity,
                        onNavigateToCreatePost = onNavigateToCreatePost,
                        onLikePost = onLikePost,
                        onOpenPostDetail = onOpenPostDetail
                    )
                    ParentNavTab.SETTINGS -> ParentSettingsScreen(
                        uiState = uiState,
                        onUpdatePin = onUpdatePin,
                        onUpdateDailyLimit = onUpdateDailyLimit,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ParentMainScreenPreview() {
    LituturTheme {
        ParentMainScreen()
    }
}
