package com.example.foss101.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foss101.data.repository.RepositoryProvider
import com.example.foss101.ui.ai.AiToolsScreen
import com.example.foss101.ui.browse.BrowseTermsScreen
import com.example.foss101.ui.categories.CategoriesScreen
import com.example.foss101.ui.chat.ChatScreen
import com.example.foss101.ui.details.TermDetailsScreen
import com.example.foss101.ui.home.HomeScreen
import com.example.foss101.ui.search.SearchScreen
import com.example.foss101.ui.settings.SettingsScreen
import com.example.foss101.ui.trendwatcher.TrendWatcherScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val glossaryRepository = remember { RepositoryProvider.glossaryRepository }

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
            route = "details/{termId}",
            arguments = listOf(
                navArgument("termId")  { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val termId = backStackEntry.arguments?.getString("termId")
            TermDetailsScreen(
                termId = termId,
                repository = glossaryRepository
            )
        }
        composable("ai_tools") { AiToolsScreen() }
        composable("trend_watcher") { TrendWatcherScreen() }
        composable("ask_glossary") { ChatScreen() }
        composable("chat") { ChatScreen() }
        composable("settings") { SettingsScreen() }
    }
}
