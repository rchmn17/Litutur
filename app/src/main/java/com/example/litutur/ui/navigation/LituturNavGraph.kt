package com.example.litutur.ui.navigation

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.litutur.ui.model.LituturScreen
import com.example.litutur.ui.parent.ParentMainScreen
import com.example.litutur.ui.parent.ParentPinDialog
import com.example.litutur.ui.screens.AlbumStikerScreen
import com.example.litutur.ui.screens.BacaCeritaScreen
import com.example.litutur.ui.screens.ChildProfileSetupScreen
import com.example.litutur.ui.screens.CreatePostScreen
import com.example.litutur.ui.screens.DashboardAnakScreen
import com.example.litutur.ui.screens.DetailBukuScreen
import com.example.litutur.ui.screens.KuisPemahamanScreen
import com.example.litutur.ui.screens.KuisSelesaiScreen
import com.example.litutur.ui.screens.LoginScreen
import com.example.litutur.ui.screens.ParentRegistrationScreen
import com.example.litutur.ui.screens.PohonKisahScreen
import com.example.litutur.ui.screens.PostDetailScreen
import com.example.litutur.ui.screens.SplashWelcomeScreen
import com.example.litutur.ui.viewmodel.AuthViewModel
import com.example.litutur.ui.viewmodel.ParentUiState
import com.example.litutur.ui.viewmodel.ParentViewModel

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object BookDetail : Screen("book_detail/{title}") {
        fun createRoute(title: String) = "book_detail/${Uri.encode(title)}"
    }
    object Reading : Screen("reading/{title}") {
        fun createRoute(title: String) = "reading/${Uri.encode(title)}"
    }
    object Quiz : Screen("quiz/{title}") {
        fun createRoute(title: String) = "quiz/${Uri.encode(title)}"
    }
    object QuizComplete : Screen("quiz_complete")
    object StoryTree : Screen("story_tree")
    object StickerAlbum : Screen("sticker_album")
}

@Composable
fun LituturNavGraph(
    navController: NavHostController = rememberNavController()
) {
    LituturAppNavigation(navController = navController)
}

@Composable
fun LituturAppNavigation(
    parentState: ParentUiState = ParentUiState(),
    onQuizCompleted: (bookTitle: String, quizScore: Int, pointsEarned: Int) -> Unit = { _, _, _ -> },
    onReturnToParentMode: () -> Unit = {},
    navController: NavHostController = rememberNavController()
) {
    var showPinDialog by remember { mutableStateOf(false) }

    val childName = parentState.selectedChild?.name?.ifBlank { "Sahabat Kecil" } ?: "Sahabat Kecil"
    val childPoints = parentState.selectedChild?.points ?: 0
    val storiesCount = parentState.readingLogs.size
    val expectedPin = parentState.parentUser?.parentPin?.ifBlank { "1234" } ?: "1234"

    if (showPinDialog) {
        ParentPinDialog(
            expectedPin = expectedPin,
            onDismiss = { showPinDialog = false },
            onSuccess = {
                showPinDialog = false
                onReturnToParentMode()
            }
        )
    }

    fun navigateChildTab(tab: Int) {
        val targetRoute = when (tab) {
            0 -> Screen.Dashboard.route
            1 -> Screen.StoryTree.route
            2, 3 -> Screen.StickerAlbum.route
            else -> Screen.Dashboard.route
        }
        navController.navigate(targetRoute) {
            popUpTo(Screen.Dashboard.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        enterTransition = { fadeIn(tween(240)) + slideInHorizontally(tween(240)) { it / 12 } },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(240)) },
        popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(200)) { it / 12 } }
    ) {
        composable(Screen.Dashboard.route) {
            BackHandler {
                showPinDialog = true
            }
            DashboardAnakScreen(
                childName = childName,
                childPoints = childPoints,
                onRequestParentMode = { showPinDialog = true },
                onNavigateToRead = { title ->
                    navController.navigate(Screen.BookDetail.createRoute(title))
                },
                onBottomTabSelected = { tab -> navigateChildTab(tab) }
            )
        }

        composable(Screen.BookDetail.route) { backStackEntry ->
            val rawTitle = backStackEntry.arguments?.getString("title") ?: "Singa yang Baik Hati"
            val bookTitle = Uri.decode(rawTitle)
            DetailBukuScreen(
                bookTitle = bookTitle,
                onBackClick = { navController.popBackStack() },
                onStartReading = { navController.navigate(Screen.Reading.createRoute(bookTitle)) }
            )
        }

        composable(Screen.Reading.route) { backStackEntry ->
            val rawTitle = backStackEntry.arguments?.getString("title") ?: "Singa yang Baik Hati"
            val bookTitle = Uri.decode(rawTitle)
            BacaCeritaScreen(
                bookTitle = bookTitle,
                onBackClick = { navController.popBackStack() },
                onNextChapter = { navController.navigate(Screen.Quiz.createRoute(bookTitle)) }
            )
        }

        composable(Screen.Quiz.route) { backStackEntry ->
            val rawTitle = backStackEntry.arguments?.getString("title") ?: "Singa yang Baik Hati"
            val bookTitle = Uri.decode(rawTitle)
            KuisPemahamanScreen(
                bookTitle = bookTitle,
                onBackClick = { navController.popBackStack() },
                onQuizComplete = { score, points ->
                    onQuizCompleted(bookTitle, score, points)
                    navController.navigate(Screen.QuizComplete.route) {
                        popUpTo(Screen.BookDetail.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.QuizComplete.route) {
            KuisSelesaiScreen(
                childName = childName,
                bookTitle = parentState.lastCompletedStoryTitle,
                quizScore = parentState.lastQuizScore,
                earnedPoints = parentState.lastEarnedPoints,
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
                childName = childName,
                childPoints = childPoints,
                storiesCount = storiesCount,
                onRequestParentMode = { showPinDialog = true },
                onNavigateToAlbum = { navigateChildTab(2) },
                onBottomTabSelected = { tab -> navigateChildTab(tab) }
            )
        }

        composable(Screen.StickerAlbum.route) {
            AlbumStikerScreen(
                childName = childName,
                childPoints = childPoints,
                storiesCount = storiesCount,
                onRequestParentMode = { showPinDialog = true },
                onBottomTabSelected = { tab -> navigateChildTab(tab) }
            )
        }
    }
}

@Composable
fun LituturNavGraph(
    authViewModel: AuthViewModel = viewModel(),
    parentViewModel: ParentViewModel = viewModel()
) {
    val context = LocalContext.current
    val authState by authViewModel.uiState.collectAsState()
    val parentState by parentViewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(LituturScreen.SPLASH_WELCOME) }
    var hasInitializedRoute by remember { mutableStateOf(false) }

    LaunchedEffect(authState.isCheckingSession, authState.isLoggedIn) {
        if (!authState.isCheckingSession && !hasInitializedRoute) {
            hasInitializedRoute = true
            if (authState.isLoggedIn) {
                if (authState.children.isEmpty()) {
                    currentScreen = LituturScreen.CHILD_PROFILE_SETUP
                } else {
                    parentViewModel.loadParentDashboard(authState.parentUser, authState.children)
                    currentScreen = LituturScreen.PARENT_MAIN
                }
            }
        }
    }

    if (authState.isCheckingSession) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Smooth hardware/gesture Back navigation for non-root screens
    BackHandler(
        enabled = currentScreen in setOf(
            LituturScreen.LOGIN,
            LituturScreen.PARENT_REGISTRATION,
            LituturScreen.CHILD_PROFILE_SETUP,
            LituturScreen.CREATE_POST,
            LituturScreen.POST_DETAIL
        )
    ) {
        authViewModel.clearMessages()
        parentViewModel.clearMessages()
        currentScreen = when (currentScreen) {
            LituturScreen.LOGIN -> LituturScreen.SPLASH_WELCOME
            LituturScreen.PARENT_REGISTRATION -> LituturScreen.LOGIN
            LituturScreen.CHILD_PROFILE_SETUP -> {
                if (authState.isLoggedIn && authState.children.isNotEmpty()) {
                    LituturScreen.PARENT_MAIN
                } else {
                    LituturScreen.PARENT_REGISTRATION
                }
            }
            LituturScreen.CREATE_POST, LituturScreen.POST_DETAIL -> LituturScreen.PARENT_MAIN
            else -> currentScreen
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(180))
        },
        label = "RootScreenTransition"
    ) { screen ->
        when (screen) {
            LituturScreen.SPLASH_WELCOME -> {
                SplashWelcomeScreen(
                    onNavigateToLogin = {
                        authViewModel.clearMessages()
                        currentScreen = LituturScreen.LOGIN
                    },
                    onNavigateToRegister = {
                        authViewModel.clearMessages()
                        currentScreen = LituturScreen.PARENT_REGISTRATION
                    },
                    onGuestMode = {
                        authViewModel.enterGuestMode()
                        parentViewModel.loadParentDashboard(null, emptyList())
                        currentScreen = LituturScreen.PARENT_MAIN
                    }
                )
            }

            LituturScreen.LOGIN -> {
                LoginScreen(
                    isLoading = authState.isLoading,
                    errorMessage = authState.errorMessage,
                    infoMessage = authState.infoMessage,
                    onBack = {
                        authViewModel.clearMessages()
                        currentScreen = LituturScreen.SPLASH_WELCOME
                    },
                    onLoginSubmit = { email, password, isParentMode ->
                        authViewModel.login(email, password) { hasChildren ->
                            if (!hasChildren) {
                                currentScreen = LituturScreen.CHILD_PROFILE_SETUP
                            } else {
                                parentViewModel.loadParentDashboard(
                                    authViewModel.uiState.value.parentUser,
                                    authViewModel.uiState.value.children
                                )
                                currentScreen = if (isParentMode) {
                                    LituturScreen.PARENT_MAIN
                                } else {
                                    LituturScreen.CHILD_MODE
                                }
                            }
                        }
                    },
                    onForgotPassword = { email ->
                        authViewModel.sendPasswordReset(email)
                    },
                    onNavigateToRegister = {
                        authViewModel.clearMessages()
                        currentScreen = LituturScreen.PARENT_REGISTRATION
                    }
                )
            }

            LituturScreen.PARENT_REGISTRATION -> {
                ParentRegistrationScreen(
                    isLoading = authState.isLoading,
                    errorMessage = authState.errorMessage,
                    onBack = {
                        authViewModel.clearMessages()
                        currentScreen = LituturScreen.LOGIN
                    },
                    onRegisterSubmit = { name, email, whatsapp, password ->
                        authViewModel.registerParent(name, email, whatsapp, password) {
                            currentScreen = LituturScreen.CHILD_PROFILE_SETUP
                        }
                    },
                    onNavigateToLogin = {
                        authViewModel.clearMessages()
                        currentScreen = LituturScreen.LOGIN
                    }
                )
            }

            LituturScreen.CHILD_PROFILE_SETUP -> {
                ChildProfileSetupScreen(
                    isLoading = authState.isLoading,
                    errorMessage = authState.errorMessage,
                    onBack = {
                        authViewModel.clearMessages()
                        currentScreen = if (authState.isLoggedIn && authState.children.isNotEmpty()) {
                            LituturScreen.PARENT_MAIN
                        } else {
                            LituturScreen.PARENT_REGISTRATION
                        }
                    },
                    onSaveProfile = { name, ageRange, avatarName, topics, photoUri ->
                        authViewModel.saveChildProfile(
                            name = name,
                            ageRange = ageRange,
                            avatarName = avatarName,
                            favoriteTopics = topics,
                            photoUri = photoUri,
                            context = context
                        ) {
                            parentViewModel.loadParentDashboard(
                                authViewModel.uiState.value.parentUser,
                                authViewModel.uiState.value.children
                            )
                            currentScreen = LituturScreen.PARENT_MAIN
                        }
                    }
                )
            }

            LituturScreen.PARENT_MAIN -> {
                ParentMainScreen(
                    uiState = parentState,
                    onSwitchToChildMode = { currentScreen = LituturScreen.CHILD_MODE },
                    onSelectChild = { parentViewModel.selectChild(it) },
                    onAddChildClick = {
                        authViewModel.clearMessages()
                        currentScreen = LituturScreen.CHILD_PROFILE_SETUP
                    },
                    onRecordReading = { title, category, minutes, score ->
                        parentViewModel.recordReadingSession(title, category, minutes, score)
                    },
                    onToggleMission = { parentViewModel.toggleMission(it) },
                    onAddCustomMission = { title, desc ->
                        parentViewModel.addCustomMission(title, desc)
                    },
                    onRefreshCommunity = { parentViewModel.refreshCommunityPosts() },
                    onNavigateToCreatePost = {
                        parentViewModel.clearMessages()
                        currentScreen = LituturScreen.CREATE_POST
                    },
                    onLikePost = { parentViewModel.toggleLikePost(it) },
                    onOpenPostDetail = { post ->
                        parentViewModel.openPostDetail(post)
                        currentScreen = LituturScreen.POST_DETAIL
                    },
                    onUpdatePin = { parentViewModel.updateParentPin(it) },
                    onUpdateDailyLimit = { parentViewModel.updateChildDailyLimit(it) },
                    onClearMessages = { parentViewModel.clearMessages() },
                    onLogout = {
                        authViewModel.logout()
                        currentScreen = LituturScreen.LOGIN
                    }
                )
            }

            LituturScreen.CHILD_MODE -> {
                LituturAppNavigation(
                    parentState = parentState,
                    onQuizCompleted = { bookTitle, quizScore, pointsEarned ->
                        parentViewModel.completeChildReadingAndQuiz(
                            storyTitle = bookTitle,
                            quizScore = quizScore,
                            pointsEarned = pointsEarned
                        )
                    },
                    onReturnToParentMode = {
                        currentScreen = LituturScreen.PARENT_MAIN
                    }
                )
            }

            LituturScreen.POST_DETAIL -> {
                PostDetailScreen(
                    uiState = parentState,
                    onBack = { currentScreen = LituturScreen.PARENT_MAIN },
                    onLikePost = { parentViewModel.toggleLikePost(it) },
                    onSendComment = { postId, text ->
                        parentViewModel.addComment(postId, text)
                    }
                )
            }

            LituturScreen.CREATE_POST -> {
                CreatePostScreen(
                    uiState = parentState,
                    onBack = { currentScreen = LituturScreen.PARENT_MAIN },
                    onPublishPost = { caption, milestone, bookTitle, photoUri ->
                        parentViewModel.createCommunityPost(
                            caption = caption,
                            selectedMilestone = milestone,
                            bookTitle = bookTitle,
                            photoUri = photoUri,
                            context = context
                        ) {
                            currentScreen = LituturScreen.PARENT_MAIN
                        }
                    }
                )
            }
        }
    }
}
