package com.example.foss101.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.foss101.data.repository.RepositoryProvider
import com.example.foss101.ui.ai.AiToolsScreen
import com.example.foss101.ui.auth.AuthScreen
import com.example.foss101.ui.browse.BrowseTermsScreen
import com.example.foss101.ui.categories.CategoriesScreen
import com.example.foss101.ui.chat.ChatScreen
import com.example.foss101.ui.details.TermDetailsScreen
import com.example.foss101.ui.draft.TermDraftScreen
import com.example.foss101.ui.home.HomeScreen
import com.example.foss101.ui.preview.TokenizationProofScreen
import com.example.foss101.ui.preview.bite.BiteFeedScreen
import com.example.foss101.ui.preview.bite.tokenizationBites
import com.example.foss101.ui.search.SearchScreen
import com.example.foss101.ui.settings.SettingsScreen
import com.example.foss101.ui.trendwatcher.TrendWatcherScreen
import com.example.foss101.viewmodel.AuthMode
import java.net.URLDecoder

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val glossaryRepository = remember { RepositoryProvider.glossaryRepository }
    val authRepository = remember { RepositoryProvider.authRepository }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(onNavigate = { route -> navController.navigate(route) })
        }
        composable("browse") {
            BrowseTermsScreen(
                onNavigate = { route -> navController.navigate(route) },
                repository = glossaryRepository
            )
        }
        composable("categories") {
            CategoriesScreen(
                onNavigate = { route -> navController.navigate(route) },
                repository = glossaryRepository
            )
        }
        composable("search") {
            SearchScreen(
                onNavigate = { route -> navController.navigate(route) },
                repository = glossaryRepository
            )
        }
        composable(
            route = "term_draft?query={query}",
            arguments = listOf(
                navArgument("query") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val encodedQuery = backStackEntry.arguments?.getString("query").orEmpty()
            val initialQuery = URLDecoder.decode(encodedQuery, Charsets.UTF_8.name())
            TermDraftScreen(
                repository = glossaryRepository,
                initialQuery = initialQuery,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "details/{termId}",
            arguments = listOf(
                navArgument("termId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val termId = backStackEntry.arguments?.getString("termId")
            TermDetailsScreen(
                termId = termId,
                repository = glossaryRepository,
                authRepository = authRepository,
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable("ai_tools") { AiToolsScreen(onNavigate = { route -> navController.navigate(route) }) }
        composable("trend_watcher") { TrendWatcherScreen() }
        composable("ask_glossary") { ChatScreen(repository = glossaryRepository) }
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
