package com.kovcom.mowid.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kovcom.mowid.base.ui.EVENTS_KEY
import com.kovcom.mowid.ui.feature.main.MainEvent
import com.kovcom.mowid.ui.feature.main.MainViewModel
import com.kovcom.mowid.ui.navigation.Navigation.Args.GROUP_ID

@Composable
fun AppNavigation(activityViewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Navigation.Route.Home.route
    ) {
        composable(
            route = Navigation.Route.Home.route
        ) {
            HomeScreenDestination(
                sendMainEvent = activityViewModel::processIntent,
                onNavigateToQuotes = { groupId ->
                    navController.navigate(route = Navigation.Route.Quotes.createRoute(groupId))
                },
                onNavigateToSettings = { navController.navigate(Navigation.Route.Settings.route) }
            )
        }
        composable(
            route = Navigation.Route.Quotes.route,
            arguments = listOf(
                navArgument(name = GROUP_ID) {
                    type = NavType.StringType
                },
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString(GROUP_ID).orEmpty()
            QuotesScreenDestination(
                groupId = groupId,
                onBackClicked = { navController.navigateUp() },
                onNavigateToSettings = { navController.navigate(Navigation.Route.Settings.route) }
            )
        }

        composable(
            route = Navigation.Route.Settings.route
        ) {
            SettingsScreenDestination(activityViewModel) { navController.navigateUp() }
        }
    }

    LaunchedEffect(EVENTS_KEY) {
        activityViewModel.event.collect { event ->
            when (event) {
                is MainEvent.NavigateToQuote -> {
                    navController.navigate(
                        route = Navigation.Route.Quotes.createRoute(event.groupId)
                    )
                }

                is MainEvent.ShowToast,
                is MainEvent.SignIn,
                is MainEvent.SignOut,
                -> {
                    // do nothing
                }
            }
        }
    }

}

object Navigation {
    data object Args {

        const val GROUP_ID = "group_id"
        const val GROUP_NAME = "group_name"
        const val QUOTE_ID = "quote_id"
    }

    sealed class Route(val route: String) {
        data object Home : Route("Home")
        data object Quotes : Route("Quotes/{$GROUP_ID}") {

            fun createRoute(groupId: String) = "Quotes/$groupId"
        }

        data object Settings : Route("Settings")
    }
}
