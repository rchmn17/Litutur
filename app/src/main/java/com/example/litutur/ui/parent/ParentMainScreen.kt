package com.example.litutur.ui.parent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.litutur.ui.model.ParentNavTab
import com.example.litutur.ui.theme.CoralOrangePrimary
import com.example.litutur.ui.theme.LituturTheme

@Composable
fun ParentMainScreen(
    onSwitchToChildMode: () -> Unit = {},
    onNavigateToCreatePost: () -> Unit = {},
    onNavigateToPostDetail: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(ParentNavTab.HOME) }

    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == ParentNavTab.HOME,
                        onClick = { selectedTab = ParentNavTab.HOME },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                        label = {
                            Text(
                                "Beranda",
                                fontWeight = if (selectedTab == ParentNavTab.HOME) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CoralOrangePrimary,
                            selectedTextColor = CoralOrangePrimary,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B),
                            indicatorColor = Color.White
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == ParentNavTab.MONITORING,
                        onClick = { selectedTab = ParentNavTab.MONITORING },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = "Monitoring") },
                        label = {
                            Text(
                                "Monitoring",
                                fontWeight = if (selectedTab == ParentNavTab.MONITORING) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CoralOrangePrimary,
                            selectedTextColor = CoralOrangePrimary,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B),
                            indicatorColor = Color.White
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == ParentNavTab.MISSIONS,
                        onClick = { selectedTab = ParentNavTab.MISSIONS },
                        icon = { Icon(Icons.Default.TaskAlt, contentDescription = "Misi") },
                        label = {
                            Text(
                                "Misi",
                                fontWeight = if (selectedTab == ParentNavTab.MISSIONS) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CoralOrangePrimary,
                            selectedTextColor = CoralOrangePrimary,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B),
                            indicatorColor = Color.White
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == ParentNavTab.COMMUNITY,
                        onClick = { selectedTab = ParentNavTab.COMMUNITY },
                        icon = { Icon(Icons.Default.Forum, contentDescription = "Komunitas") },
                        label = {
                            Text(
                                "Komunitas",
                                fontWeight = if (selectedTab == ParentNavTab.COMMUNITY) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CoralOrangePrimary,
                            selectedTextColor = CoralOrangePrimary,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B),
                            indicatorColor = Color.White
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == ParentNavTab.SETTINGS,
                        onClick = { selectedTab = ParentNavTab.SETTINGS },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Pengaturan") },
                        label = {
                            Text(
                                "Pengaturan",
                                fontWeight = if (selectedTab == ParentNavTab.SETTINGS) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CoralOrangePrimary,
                            selectedTextColor = CoralOrangePrimary,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B),
                            indicatorColor = Color.White
                        )
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
            when (selectedTab) {
                ParentNavTab.HOME -> ParentHomeScreen(onSwitchToChildMode = onSwitchToChildMode)
                ParentNavTab.MONITORING -> ParentAnalyticsScreen()
                ParentNavTab.MISSIONS -> ParentMissionsScreen()
                ParentNavTab.COMMUNITY -> ParentCommunityScreen(
                    onNavigateToCreatePost = onNavigateToCreatePost,
                    onNavigateToPostDetail = onNavigateToPostDetail
                )
                ParentNavTab.SETTINGS -> ParentSettingsScreen(onLogout = onLogout)
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
