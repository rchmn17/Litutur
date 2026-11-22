package com.example.litutur.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.litutur.ui.screens.*

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object BookDetail : Screen("book_detail/{title}") {
        fun createRoute(title: String) = "book_detail/$title"
    }
    object Reading : Screen("reading/{title}") {
        fun createRoute(title: String) = "reading/$title"
    }
    object Quiz : Screen("quiz")
    object QuizComplete : Screen("quiz_complete")
    object StoryTree : Screen("story_tree")
    object StickerAlbum : Screen("sticker_album")
}

@Composable
fun LituturAppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardAnakScreen(
                onNavigateToRead = { title ->
                    navController.navigate(Screen.BookDetail.createRoute(title))
                },
                onBottomTabSelected = { tab ->
                    when (tab) {
                        1 -> navController.navigate(Screen.StoryTree.route)
                        2 -> navController.navigate(Screen.StickerAlbum.route)
                    }
                }
            )
        }

        composable(Screen.BookDetail.route) { backStackEntry ->
            val bookTitle = backStackEntry.arguments?.getString("title") ?: "Singa yang Baik Hati"
            DetailBukuScreen(
                bookTitle = bookTitle,
                onBackClick = { navController.popBackStack() },
                onStartReading = { navController.navigate(Screen.Reading.createRoute(bookTitle)) }
            )
        }

        composable(Screen.Reading.route) {
            BacaCeritaScreen(
                onBackClick = { navController.popBackStack() },
                onNextChapter = { navController.navigate(Screen.Quiz.route) }
            )
        }

        composable(Screen.Quiz.route) {
            KuisPemahamanScreen(
                onBackClick = { navController.popBackStack() },
                onQuizComplete = { navController.navigate(Screen.QuizComplete.route) }
            )
        }

        composable(Screen.QuizComplete.route) {
            KuisSelesaiScreen(
                onClaimReward = {
                    navController.navigate(Screen.StoryTree.route) {
                        popUpTo(Screen.Dashboard.route)
                    }
                },
                onReadMoreBooks = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.StoryTree.route) {
            PohonKisahScreen(
                onNavigateToAlbum = { navController.navigate(Screen.StickerAlbum.route) },
                onBottomTabSelected = { tab ->
                    when (tab) {
                        0 -> navController.navigate(Screen.Dashboard.route)
                        2 -> navController.navigate(Screen.StickerAlbum.route)
                    }
                }
            )
        }

        composable(Screen.StickerAlbum.route) {
            AlbumStikerScreen(
                onBottomTabSelected = { tab ->
                    when (tab) {
                        0 -> navController.navigate(Screen.Dashboard.route)
                        1 -> navController.navigate(Screen.StoryTree.route)
                    }
                }
            )
        }
    }
}
