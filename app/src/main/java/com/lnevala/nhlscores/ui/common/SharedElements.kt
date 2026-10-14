package com.lnevala.nhlscores.ui.common

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Makes a game's card in the list and its detail screen one container that grows and shrinks
 * during navigation. Does nothing outside navigation, e.g. in previews.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.gameContainer(gameId: Long, shape: Shape): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current ?: return this
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(sharedTransitionScope) {
        this@gameContainer.sharedBounds(
            sharedContentState = rememberSharedContentState(key = "game-$gameId"),
            animatedVisibilityScope = animatedVisibilityScope,
            clipInOverlayDuringTransition = OverlayClip(shape),
        )
    }
}
