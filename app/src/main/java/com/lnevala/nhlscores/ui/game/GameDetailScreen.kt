package com.lnevala.nhlscores.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lnevala.nhlscores.R
import com.lnevala.nhlscores.domain.isLive
import com.lnevala.nhlscores.ui.common.CenteredMessage
import com.lnevala.nhlscores.ui.common.LoadError
import com.lnevala.nhlscores.ui.common.RefreshWhileLive
import com.lnevala.nhlscores.ui.common.gameContainer
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun GameDetailScreen(
    gameId: Long,
    onBack: () -> Unit,
    viewModel: GameDetailViewModel = viewModel(factory = GameDetailViewModel.factory(gameId)),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    RefreshWhileLive(enabled = state.detail?.game?.isLive == true, onRefresh = viewModel::refreshQuietly)

    GameDetailContent(
        gameId = gameId,
        state = state,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameDetailContent(
    gameId: Long,
    state: GameDetailUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
) {
    val detail = state.detail

    Scaffold(
        modifier = Modifier.gameContainer(gameId, RectangleShape),
        topBar = {
            TopAppBar(
                title = {
                    detail?.let { Text(it.gameDay.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))) }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding()),
        ) {
            when {
                detail != null -> LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = padding.calculateBottomPadding() + 16.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "header") { GameHeader(detail) }
                    if (detail.preview != null) {
                        previewSections(detail, detail.preview)
                    } else {
                        playedGameSections(detail, state.followed)
                    }
                }
                state.isLoading -> CenteredMessage { CircularProgressIndicator() }
                else -> LoadError(onRetry)
            }
        }
    }
}
