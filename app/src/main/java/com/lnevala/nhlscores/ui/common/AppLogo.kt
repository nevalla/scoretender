package com.lnevala.nhlscores.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import com.lnevala.nhlscores.R

/**
 * The launcher icon drawn in the app, from the same two layers. Launchers show only the middle
 * 72 of an adaptive icon's 108dp, so the layers are scaled up to match.
 */
@Composable
fun AppLogo(modifier: Modifier = Modifier) {
    Box(modifier.clip(CircleShape)) {
        listOf(R.drawable.ic_launcher_background, R.drawable.ic_launcher_foreground).forEach {
            Image(
                painter = painterResource(it),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(ADAPTIVE_ICON_SCALE),
            )
        }
    }
}

private const val ADAPTIVE_ICON_SCALE = 108f / 72f
