package org.dishch.tabletopaiassistant.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.dishch.tabletopaiassistant.feature.assistant.presentation.ui.AssistantScreen
import org.dishch.tabletopaiassistant.feature.settings.presentation.ui.SettingsScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavigationConfig.Chat,
    ) {
        composable<NavigationConfig.Chat> {
            AssistantScreen(
                viewModel = hiltViewModel(),
                onNavigateToSettings = { navController.navigate(NavigationConfig.Settings) },
            )
        }

        composable<NavigationConfig.Settings> {
            SettingsScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
