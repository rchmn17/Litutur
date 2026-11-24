package com.example.litutur.ui.navigation

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
import com.example.litutur.ui.screens.*
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
import com.example.litutur.ui.viewmodel.ParentViewModel

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

    when (currentScreen) {
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
                        } else if (isParentMode) {
                            parentViewModel.loadParentDashboard(
                                authViewModel.uiState.value.parentUser,
                                authViewModel.uiState.value.children
                            )
                            currentScreen = LituturScreen.PARENT_MAIN
                        } else {
                            currentScreen = LituturScreen.CHILD_MODE
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
                onLogout = {
                    authViewModel.logout()
                    currentScreen = LituturScreen.LOGIN
                }
            )
        }

        LituturScreen.CHILD_MODE -> {
            LituturAppNavigation()
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
