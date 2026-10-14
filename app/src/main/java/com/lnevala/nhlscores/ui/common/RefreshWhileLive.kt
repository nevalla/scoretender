package com.lnevala.nhlscores.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

private val LiveRefreshInterval = 15.seconds

/** Polls only while a game is live and the screen is visible. */
@Composable
fun RefreshWhileLive(enabled: Boolean, onRefresh: () -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnRefresh by rememberUpdatedState(onRefresh)

    LaunchedEffect(enabled, lifecycle) {
        if (!enabled) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                delay(LiveRefreshInterval)
                currentOnRefresh()
            }
        }
    }
}
