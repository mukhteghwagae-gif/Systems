package systems.nzr1.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import systems.nzr1.ui.screens.apps.AppsScreen
import systems.nzr1.ui.screens.assistant.AssistantScreen
import systems.nzr1.ui.screens.boot.BootScreen
import systems.nzr1.ui.screens.dashboard.DashboardScreen
import systems.nzr1.ui.screens.files.FilesScreen
import systems.nzr1.ui.screens.monitor.MonitorScreen
import systems.nzr1.ui.screens.network.NetworkScreen
import systems.nzr1.ui.screens.security.SecurityScreen
import systems.nzr1.ui.screens.settings.SettingsScreen
import systems.nzr1.ui.screens.terminal.TerminalScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SystemsNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController   = navController,
        startDestination = Screen.Boot.route,
        modifier        = modifier,
        enterTransition  = {
            fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 }
        },
        exitTransition   = {
            fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 4 }
        },
        popEnterTransition = {
            fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it / 4 }
        },
        popExitTransition  = {
            fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { it / 4 }
        },
    ) {
        composable(Screen.Boot.route)      { BootScreen(navController) }
        composable(Screen.Dashboard.route) { DashboardScreen(navController) }
        composable(Screen.Monitor.route)   { MonitorScreen() }
        composable(Screen.Network.route)   { NetworkScreen() }
        composable(Screen.Security.route)  { SecurityScreen() }
        composable(Screen.Assistant.route) { AssistantScreen() }
        composable(Screen.Files.route)     { FilesScreen() }
        composable(Screen.Apps.route)      { AppsScreen() }
        composable(Screen.Terminal.route)  { TerminalScreen() }
        composable(Screen.Settings.route)  { SettingsScreen() }
    }
}
