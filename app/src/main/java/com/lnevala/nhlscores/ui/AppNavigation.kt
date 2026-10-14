package com.lnevala.nhlscores.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.lnevala.nhlscores.ui.common.LocalNavAnimatedVisibilityScope
import com.lnevala.nhlscores.ui.common.LocalSharedTransitionScope
import com.lnevala.nhlscores.ui.game.GameDetailScreen
import com.lnevala.nhlscores.ui.scores.ScoresScreen
import kotlinx.serialization.Serializable

@Serializable
private data object ScoresRoute

@Serializable
private data class GameRoute(val gameId: Long)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(navController = navController, startDestination = ScoresRoute) {
                // Navigating only from a resumed screen ignores double taps made during a transition.
                composable<ScoresRoute> { entry ->
                    CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                        ScoresScreen(
                            onGameClick = { gameId ->
                                if (entry.lifecycle.currentState == Lifecycle.State.RESUMED) {
                                    navController.navigate(GameRoute(gameId))
                                }
                            },
                        )
                    }
                }
                composable<GameRoute> { entry ->
                    CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                        GameDetailScreen(
                            gameId = entry.toRoute<GameRoute>().gameId,
                            onBack = dropUnlessResumed { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}
