package com.lnevala.nhlscores.ui.game

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

data class TeamColors(val away: Color, val home: Color)

/**
 * The away team gets the theme's primary colour and the home team the same tone with the hue turned,
 * so the two are easy to tell apart yet stay as soft as the rest of the theme, in light and dark.
 */
@Composable
fun teamColors(): TeamColors {
    val primary = MaterialTheme.colorScheme.primary
    return TeamColors(away = primary, home = primary.withHueTurned(HOME_HUE_TURN))
}

private const val HOME_HUE_TURN = 120f

private fun Color.withHueTurned(degrees: Float): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(toArgb(), hsl)
    return Color.hsl((hsl[0] + degrees) % 360f, hsl[1], hsl[2], alpha)
}
