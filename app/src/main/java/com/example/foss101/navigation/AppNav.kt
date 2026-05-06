package com.example.foss101.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foss101.data.repository.RepositoryProvider
import com.example.foss101.ui.auth.AuthScreen
import com.example.foss101.ui.home.HomeScreen
import com.example.foss101.ui.library.GlossaryLibraryScreen
import com.example.foss101.ui.preview.TokenizationProofScreen
import com.example.foss101.ui.preview.bite.BiteFeedScreen
import com.example.foss101.ui.preview.bite.tokenizationBites
import com.example.foss101.ui.settings.SettingsScreen
import com.example.foss101.viewmodel.AuthMode

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val glossaryRepository = remember { RepositoryProvider.glossaryRepository }
    val authRepository = remember { RepositoryProvider.authRepository }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(onNavigate = { route -> navController.navigate(route) })
        }
        composable("glossary") {
            GlossaryLibraryScreen(repository = glossaryRepository)
        }
        composable("settings") {
            SettingsScreen(
                authRepository = authRepository,
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable("auth_login") {
            AuthScreen(
                initialMode = AuthMode.Login,
                authRepository = authRepository,
                onBack = { navController.popBackStack() },
                onAuthenticated = { navController.popBackStack() }
            )
        }
        composable("auth_signup") {
            AuthScreen(
                initialMode = AuthMode.Signup,
                authRepository = authRepository,
                onBack = { navController.popBackStack() },
                onAuthenticated = { navController.popBackStack() }
            )
        }
        composable("preview_tokenization") {
            TokenizationProofScreen(onBack = { navController.popBackStack() })
        }
        composable("preview_tokenization_bite") {
            BiteFeedScreen(
                bites = tokenizationBites(),
                onClose = { navController.popBackStack() }
            )
        }
    }
}
