package com.example.voltix.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView

// Централизованная палитра Voltix: светлая и тёмная темы.
// Экраны берут цвета отсюда (VoltixPalette.*), поэтому переключение темы
// меняет весь интерфейс сразу.

object ThemeMode {
    const val SYSTEM = "SYSTEM"
    const val LIGHT = "LIGHT"
    const val DARK = "DARK"
}

val VoltixLightScheme = lightColorScheme(
    primary = VoltixTeal,
    onPrimary = Color.White,
    secondary = VoltixOrange,
    background = VoltixBackground,
    surface = VoltixSurface
)

val VoltixDarkScheme = darkColorScheme(
    primary = Color(0xFF7FD8C9),
    onPrimary = VoltixTealDark,
    secondary = VoltixOrangeLight,
    background = Color(0xFF121216),
    surface = Color(0xFF1E1E24),
    onBackground = Color(0xFFECECEC),
    onSurface = Color(0xFFECECEC)
)

/**
 * Семантические цвета текущего режима темы. Все экраны используют их вместо
 * захардкоженных констант, чтобы UI корректно выглядел и в светлой, и в тёмной теме.
 */
@Composable
fun voltixPalette(): VoltixColors {
    val explicit = LocalThemeMode.current
    val dark = when (explicit) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        else -> isSystemInDarkTheme()
    }
    return if (dark) DarkVoltixColors else LightVoltixColors
}

data class VoltixColors(
    val background: Color,
    val card: Color,
    val chipUnselected: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val divider: Color,
    val outline: Color,
    val fieldBackground: Color,
    val bottomBar: Color,
    val navInactive: Color,
    val teal: Color,
    val orange: Color,
    val red: Color,
    val green: Color,
    val blue: Color,
    val purple: Color,
    val amber: Color,
    val stageDeliveredBg: Color,
    val stageTransitBg: Color,
    val stageAssemblingBg: Color,
    val stageDeliveredFg: Color,
    val stageTransitFg: Color,
    val stageAssemblingFg: Color,
    val tealSoft: Color,
    val scrim: Color
)

val LightVoltixColors = VoltixColors(
    background = VoltixBackground,            // #F8F9FA
    card = Color.White,                       // карточки/плитки
    chipUnselected = Color(0xFFF3F4F6),       // невыбранные чипы и поля ввода
    textPrimary = VoltixTextPrimary,          // #1E1E1E
    textSecondary = VoltixTextSecondary,      // #757575
    textTertiary = Color(0xFF616161),
    divider = Color(0xFFE0E0E0),
    outline = Color(0xFFD1D5DB),
    fieldBackground = Color(0xFFF3F4F6),
    bottomBar = Color(0xFF1C1C22),            // фирменная тёмная нижняя панель в обеих темах
    navInactive = Color(0xFF8A8A93),
    teal = VoltixTeal,
    orange = VoltixOrange,
    red = Color(0xFFE53935),
    green = Color(0xFF2E7D32),
    blue = Color(0xFF2563EB),
    purple = Color(0xFF7C3AED),
    amber = Color(0xFFFFC107),
    stageDeliveredBg = Color(0xFFE8F5E9),
    stageTransitBg = Color(0xFFE3F2FD),
    stageAssemblingBg = Color(0xFFFFF3E0),
    stageDeliveredFg = Color(0xFF2E7D32),
    stageTransitFg = Color(0xFF1976D2),
    stageAssemblingFg = Color(0xFFEF6C00),
    tealSoft = Color(0xFFE9F1F2),
    scrim = Color(0x73000000)
)

val DarkVoltixColors = VoltixColors(
    background = Color(0xFF121216),
    card = Color(0xFF1E1E24),
    chipUnselected = Color(0xFF2A2A33),
    textPrimary = Color(0xFFECECEC),
    textSecondary = Color(0xFFB9B9C3),
    textTertiary = Color(0xFFA8A8B3),
    divider = Color(0xFF3A3A42),
    outline = Color(0xFF4A4A55),
    fieldBackground = Color(0xFF2A2A33),
    bottomBar = Color(0xFF0B0B0F),
    navInactive = Color(0xFF8A8A93),
    teal = Color(0xFF7FD8C9),                 // светлый мятный акцент на тёмном фоне
    orange = VoltixOrangeLight,
    red = Color(0xFFFF6B6B),
    green = Color(0xFF66D07A),
    blue = Color(0xFF6EA8FE),
    purple = Color(0xFFB79CFF),
    amber = Color(0xFFFFD54F),
    stageDeliveredBg = Color(0xFF1B3A24),
    stageTransitBg = Color(0xFF16324F),
    stageAssemblingBg = Color(0xFF3D2A14),
    stageDeliveredFg = Color(0xFF81E08F),
    stageTransitFg = Color(0xFF8AB8F8),
    stageAssemblingFg = Color(0xFFFFB877),
    tealSoft = Color(0xFF1E3A40),
    scrim = Color(0x99000000)
)

/** Режим темы, выбранный пользователем (LIGHT / DARK / SYSTEM). По умолчанию — как система. */
val LocalThemeMode = staticCompositionLocalOf { ThemeMode.SYSTEM }

@Composable
fun VoltixTheme(
    themeMode: String = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val explicit = themeMode
    val dark = when (explicit) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        else -> isSystemInDarkTheme()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor =
                if (dark) 0xFF121216.toInt() else 0xFFF8F9FA.toInt()
        }
    }
    CompositionLocalProvider(LocalThemeMode provides explicit) {
        MaterialTheme(
            colorScheme = if (dark) VoltixDarkScheme else VoltixLightScheme,
            content = content
        )
    }
}

/** Текст поверх акцентных (насыщенных) плашек: белый в светлой теме, почти чёрный в тёмной. */
@Composable
fun onAccentWhite(): Color {
    val explicit = LocalThemeMode.current
    val dark = when (explicit) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        else -> isSystemInDarkTheme()
    }
    return if (dark) Color(0xFF121216) else Color.White
}
