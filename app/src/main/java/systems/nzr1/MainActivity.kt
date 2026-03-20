package systems.nzr1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import systems.nzr1.ui.components.SystemsBottomBar
import systems.nzr1.ui.navigation.NavGraph
import systems.nzr1.ui.navigation.Screen
import systems.nzr1.ui.theme.DeepVoid
import systems.nzr1.ui.theme.SystemsTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val noNavRoutes = setOf(Screen.Boot.route)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SystemsTheme {
                val navController = rememberNavController()
                val backStack     by navController.currentBackStackEntryAsState()
                val currentRoute  = backStack?.destination?.route

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DeepVoid)
                        .windowInsetsPadding(WindowInsets.systemBars)
                ) {
                    NavGraph(
                        navController = navController,
                        modifier      = Modifier.fillMaxSize(),
                    )

                    if (currentRoute !in noNavRoutes) {
                        SystemsBottomBar(
                            currentRoute = currentRoute,
                            onNavigate   = { screen ->
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Dashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        )
                    }
                }
            }
        }
    }
}
