package com.sharjeel.wtmp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sharjeel.wtmp.repository.UserPreferencesRepository
import com.sharjeel.wtmp.ui.screens.dashboard.DashboardScreen
import com.sharjeel.wtmp.ui.screens.history.EventDetailsScreen
import com.sharjeel.wtmp.ui.screens.history.HistoryScreen
import com.sharjeel.wtmp.ui.screens.onboarding.OnboardingScreen
import com.sharjeel.wtmp.ui.screens.privacy.PrivacyCenterScreen
import com.sharjeel.wtmp.ui.screens.settings.SettingsScreen
import com.sharjeel.wtmp.ui.screens.splash.SplashScreen
import com.sharjeel.wtmp.ui.screens.stats.StatsScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object History : Screen("history")
    object EventDetails : Screen("event_details/{eventId}") {
        fun createRoute(eventId: String) = "event_details/$eventId"
    }
    object Settings : Screen("settings")
    object Stats : Screen("stats")
    object PrivacyCenter : Screen("privacy_center")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    userPreferencesRepository: UserPreferencesRepository
) {
    val hasCompletedOnboarding by userPreferencesRepository.hasCompletedOnboarding.collectAsState(initial = false)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onNavigationToOnboarding = {
                val targetDestination = if (hasCompletedOnboarding) {
                    Screen.Dashboard.route
                } else {
                    Screen.Onboarding.route
                }

                navController.navigate(targetDestination) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onNavigationToDashboard = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToEventDetails = { eventId ->
                    navController.navigate(Screen.EventDetails.createRoute(eventId))
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateToDetails = { eventId ->
                    navController.navigate(Screen.EventDetails.createRoute(eventId))
                }
            )
        }
        composable(
            route = Screen.EventDetails.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailsScreen(
                eventId = eventId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Stats.route) {
            StatsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.PrivacyCenter.route) {
            PrivacyCenterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}