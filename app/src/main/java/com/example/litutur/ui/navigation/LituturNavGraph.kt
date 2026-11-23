package com.example.litutur.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.litutur.ui.model.LituturScreen
import com.example.litutur.ui.parent.ParentMainScreen
import com.example.litutur.ui.screens.ChildProfileSetupScreen
import com.example.litutur.ui.screens.CreatePostScreen
import com.example.litutur.ui.screens.LoginScreen
import com.example.litutur.ui.screens.ParentRegistrationScreen
import com.example.litutur.ui.screens.PostDetailScreen
import com.example.litutur.ui.screens.SplashWelcomeScreen

@Composable
fun LituturNavGraph() {
    var currentScreen by remember { mutableStateOf(LituturScreen.SPLASH_WELCOME) }

    when (currentScreen) {
        LituturScreen.SPLASH_WELCOME -> {
            SplashWelcomeScreen(
                onNavigateToLogin = { currentScreen = LituturScreen.LOGIN },
                onNavigateToRegister = { currentScreen = LituturScreen.PARENT_REGISTRATION },
                onGuestMode = { currentScreen = LituturScreen.PARENT_MAIN }
            )
        }

        LituturScreen.LOGIN -> {
            LoginScreen(
                onBack = { currentScreen = LituturScreen.SPLASH_WELCOME },
                onLoginSuccess = { currentScreen = LituturScreen.PARENT_MAIN },
                onNavigateToRegister = { currentScreen = LituturScreen.PARENT_REGISTRATION }
            )
        }

        LituturScreen.PARENT_REGISTRATION -> {
            ParentRegistrationScreen(
                onBack = { currentScreen = LituturScreen.LOGIN },
                onNextToChildProfile = { currentScreen = LituturScreen.CHILD_PROFILE_SETUP },
                onNavigateToLogin = { currentScreen = LituturScreen.LOGIN }
            )
        }

        LituturScreen.CHILD_PROFILE_SETUP -> {
            ChildProfileSetupScreen(
                onBack = { currentScreen = LituturScreen.PARENT_REGISTRATION },
                onStartAdventure = { currentScreen = LituturScreen.PARENT_MAIN }
            )
        }

        LituturScreen.PARENT_MAIN -> {
            ParentMainScreen(
                onSwitchToChildMode = { currentScreen = LituturScreen.SPLASH_WELCOME },
                onNavigateToCreatePost = { currentScreen = LituturScreen.CREATE_POST },
                onNavigateToPostDetail = { currentScreen = LituturScreen.POST_DETAIL },
                onLogout = { currentScreen = LituturScreen.LOGIN }
            )
        }

        LituturScreen.POST_DETAIL -> {
            PostDetailScreen(
                onBack = { currentScreen = LituturScreen.PARENT_MAIN }
            )
        }

        LituturScreen.CREATE_POST -> {
            CreatePostScreen(
                onBack = { currentScreen = LituturScreen.PARENT_MAIN },
                onPublishSuccess = { currentScreen = LituturScreen.PARENT_MAIN }
            )
        }
    }
}
