package systems.nzr1.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val SystemsTypography = Typography(
    displayLarge = TextStyle(
        fontFamily   = FontFamily.Monospace,
        fontWeight   = FontWeight.Black,
        fontSize     = 48.sp,
        lineHeight   = 56.sp,
        letterSpacing = 2.sp,
    ),
    displayMedium = TextStyle(
        fontFamily   = FontFamily.Monospace,
        fontWeight   = FontWeight.Bold,
        fontSize     = 36.sp,
        lineHeight   = 44.sp,
        letterSpacing = 1.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily   = FontFamily.Monospace,
        fontWeight   = FontWeight.Bold,
        fontSize     = 28.sp,
        lineHeight   = 36.sp,
        letterSpacing = 1.5.sp,
        color        = CyanCore,
    ),
    headlineMedium = TextStyle(
        fontFamily   = FontFamily.Monospace,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 22.sp,
        lineHeight   = 30.sp,
        letterSpacing = 1.sp,
        color        = CyanCore,
    ),
    headlineSmall = TextStyle(
        fontFamily   = FontFamily.Monospace,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 18.sp,
        lineHeight   = 26.sp,
        letterSpacing = 0.8.sp,
        color        = TextPrimary,
    ),
    titleLarge = TextStyle(
        fontFamily   = FontFamily.Monospace,
        fontWeight   = FontWeight.Bold,
        fontSize     = 16.sp,
        lineHeight   = 24.sp,
        letterSpacing = 1.2.sp,
        color        = CyanCore,
    ),
    titleMedium = TextStyle(
        fontFamily   = FontFamily.Monospace,
        fontWeight   = FontWeight.Medium,
        fontSize     = 14.sp,
        lineHeight   = 22.sp,
        letterSpacing = 0.8.sp,
        color        = TextPrimary,
    ),
    titleSmall = TextStyle(
        fontFamily   = FontFamily.Monospace,
        fontWeight   = FontWeight.Medium,
        fontSize     = 12.sp,
        lineHeight   = 18.sp,
        letterSpacing = 0.5.sp,
        color        = TextSecondary,
    ),
    bodyLarge = TextStyle(
        fontFamily   = FontFamily.Monospace,
        fontWeight   = FontWeight.Normal,
        fontSize     = 14.sp,
        lineHeight   = 22.sp,
        letterSpacing = 0.3.sp,
        color        = TextPrimary,
    ),
    bodyMedium = TextStyle(
        fontFamily   = FontFamily.Monospace,
        fontWeight   = FontWeight.Normal,
        fontSize     = 12.sp,
        lineHeight   = 18.sp,
        letterSpacing = 0.2.sp,
        color        = TextSecondary,
    ),
    bodySmall = TextStyle(
        fontFamily   = FontFamily.Monospace,
        fontWeight   = FontWeight.Normal,
        fontSize     = 10.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.4.sp,
        color        = TextMuted,
    ),
    labelLarge = TextStyle(
        fontFamily   = FontFamily.Monospace,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing = 1.5.sp,
        color        = CyanCore,
    ),
    labelMedium = TextStyle(
        fontFamily   = FontFamily.Monospace,
        fontWeight   = FontWeight.Medium,
        fontSize     = 10.sp,
        lineHeight   = 14.sp,
        letterSpacing = 1.2.sp,
        color        = TextSecondary,
    ),
    labelSmall = TextStyle(
        fontFamily   = FontFamily.Monospace,
        fontWeight   = FontWeight.Normal,
        fontSize     = 9.sp,
        lineHeight   = 12.sp,
        letterSpacing = 0.8.sp,
        color        = TextMuted,
    ),
)
