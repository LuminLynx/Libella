package com.example.foss101.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.foss101.data.repository.RepositoryProvider
import com.example.foss101.ui.auth.AuthScreen
import com.example.foss101.ui.library.GlossaryLibraryScreen
import com.example.foss101.ui.path.PathHomeScreen
import com.example.foss101.ui.preview.TokenizationProofScreen
import com.example.foss101.ui.preview.bite.BiteFeedScreen
import com.example.foss101.ui.preview.bite.tokenizationBites
import com.example.foss101.ui.settings.SettingsScreen
import com.example.foss101.ui.unit.UnitReaderScreen
import com.example.foss101.viewmodel.AuthMode

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val glossaryRepository = remember { RepositoryProvider.glossaryRepository }
    val authRepository = remember { RepositoryProvider.authRepository }
    val pathRepository = remember { RepositoryProvider.pathRepository }
    val completionCache = remember { RepositoryProvider.completionCacheInstance }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            PathHomeScreen(
                pathRepository = pathRepository,
                completionCache = completionCache,
                onOpenUnit = { unitId -> navController.navigate("unit/$unitId") },
                onOpenSettings = { navController.navigate("settings") },
                onAuthExpired = { navController.navigate("auth_login") }
            )
        }
        composable(
            route = "unit/{unitId}",
            arguments = listOf(navArgument("unitId") { type = NavType.StringType })
        ) { backStackEntry ->
            val unitId = backStackEntry.arguments?.getString("unitId").orEmpty()
            UnitReaderScreen(
                pathRepository = pathRepository,
                completionCache = completionCache,
                unitId = unitId,
                onAuthExpired = {
                    // Pop the unit reader off the back stack on the way to
                    // auth_login. Otherwise hitting back from the sign-in
                    // screen drops the user back onto the unit reader's
                    // lingering Error(authExpired) state, which looks like
                    // a server error to the user. After auth they re-tap
                    // Continue from path home — one extra tap, no confusing
                    // error screen.
                    navController.navigate("auth_login") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
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
