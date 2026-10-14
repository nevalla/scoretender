package com.lnevala.nhlscores.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance
import com.lnevala.nhlscores.domain.TeamLine

/** The NHL draws separate logos for light and dark backgrounds. */
@Composable
fun TeamLine.logoForTheme(): String =
    if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) darkLogoUrl else logoUrl
