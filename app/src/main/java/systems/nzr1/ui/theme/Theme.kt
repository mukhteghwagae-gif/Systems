package systems.nzr1.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SystemsDarkColorScheme = darkColorScheme(
    primary              = CyanCore,
    onPrimary            = DeepVoid,
    primaryContainer     = CyanDark,
    onPrimaryContainer   = CyanBright,
    secondary            = PurpleCore,
    onSecondary          = DeepVoid,
    secondaryContainer   = PurpleDim,
    onSecondaryContainer = PurpleBright,
    tertiary             = PinkCore,
    onTertiary           = DeepVoid,
    tertiaryContainer    = Color(0xFF5A0020),
    onTertiaryContainer  = PinkBright,
    background           = DeepVoid,
    onBackground         = TextPrimary,
    surface              = SurfaceDark,
    onSurface            = TextPrimary,
    surfaceVariant       = SurfaceElevated,
    onSurfaceVariant     = TextSecondary,
    surfaceTint          = CyanGlow10,
    outline              = BorderDim,
    outlineVariant       = BorderBright,
    error                = RedCore,
    onError              = DeepVoid,
    errorContainer       = Color(0xFF5A0010),
    onErrorContainer     = RedBright,
    inverseSurface       = TextPrimary,
    inverseOnSurface     = DeepVoid,
    inversePrimary       = CyanDark,
    scrim                = Color(0xCC000010),
)

@Composable
fun SystemsTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor     = DeepVoid.toArgb()
            window.navigationBarColor = DeepVoid.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars    = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(
        colorScheme = SystemsDarkColorScheme,
        typography  = SystemsTypography,
        content     = content
    )
}
