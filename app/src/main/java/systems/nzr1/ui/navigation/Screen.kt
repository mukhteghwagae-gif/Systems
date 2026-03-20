package systems.nzr1.ui.navigation

sealed class Screen(val route: String) {
    object Boot      : Screen("boot")
    object Dashboard : Screen("dashboard")
    object Monitor   : Screen("monitor")
    object Network   : Screen("network")
    object Security  : Screen("security")
    object Assistant : Screen("assistant")
    object Files     : Screen("files")
    object Apps      : Screen("apps")
    object Terminal  : Screen("terminal")
    object Settings  : Screen("settings")
}
