package com.lnevala.nhlscores.ui.scores

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lnevala.nhlscores.R
import com.lnevala.nhlscores.domain.Country
import com.lnevala.nhlscores.domain.FollowedPlayers
import com.lnevala.nhlscores.domain.Game
import com.lnevala.nhlscores.domain.isLive
import com.lnevala.nhlscores.ui.common.AppLogo
import com.lnevala.nhlscores.ui.common.CenteredMessage
import com.lnevala.nhlscores.ui.common.LoadError
import com.lnevala.nhlscores.ui.common.RefreshWhileLive
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@Composable
fun ScoresScreen(
    onGameClick: (gameId: Long) -> Unit,
    viewModel: ScoresViewModel = viewModel(factory = ScoresViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    RefreshWhileLive(enabled = state.games.any { it.isLive }, onRefresh = viewModel::refreshQuietly)

    ScoresContent(
        state = state,
        onSelectDate = viewModel::selectDate,
        onFollowCountry = viewModel::follow,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
        onGameClick = onGameClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScoresContent(
    state: ScoresUiState,
    onSelectDate: (LocalDate) -> Unit,
    onFollowCountry: (Country?) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onGameClick: (gameId: Long) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val pagerState = rememberPagerState { state.days.size }
    val scope = rememberCoroutineScope()

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showCountryPicker by rememberSaveable { mutableStateOf(false) }

    SyncPagerWithSelection(pagerState, state.days, state.selectedDate, onSelectDate)

    if (showDatePicker && state.selectedDate != null) {
        DayPickerDialog(
            initialDate = state.selectedDate,
            onDismiss = { showDatePicker = false },
            onDatePicked = onSelectDate,
        )
    }
    if (showCountryPicker) {
        CountryPickerDialog(
            selected = state.followed.country,
            onDismiss = { showCountryPicker = false },
            onCountryPicked = onFollowCountry,
        )
    }

    Scaffold(
        topBar = {
            // One surface behind both the app bar and the tabs, so they tint together when the list scrolls under.
            Surface(color = headerColor(scrollBehavior.state.overlappedFraction)) {
                Column {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                AppLogo(Modifier.size(40.dp))
                                Column {
                                    Text(stringResource(R.string.app_name))
                                    LiveSummary(state.games.count { it.isLive })
                                }
                            }
                        },
                        actions = {
                            val chooseCountry = stringResource(R.string.choose_country)
                            IconButton(
                                onClick = { showCountryPicker = true },
                                modifier = Modifier.semantics { contentDescription = chooseCountry },
                            ) {
                                CountryMark(state.followed.country)
                            }
                            if (state.selectedDate != null) {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.pick_date))
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent,
                        ),
                        scrollBehavior = scrollBehavior,
                    )
                    if (state.days.isNotEmpty() && state.today != null) {
                        DayTabs(
                            days = state.days,
                            gameCounts = state.gameCounts,
                            selectedIndex = pagerState.currentPage,
                            today = state.today,
                            onSelect = { scope.launch { pagerState.animateScrollToPage(it) } },
                        )
                    }
                }
            }
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
                state.days.isNotEmpty() -> HorizontalPager(pagerState) { page ->
                    val date = state.days[page]
                    val games = state.gamesByDate[date]
                    when {
                        games == null && date == state.selectedDate && state.hasError -> LoadError(onRetry)
                        games == null -> CenteredMessage { CircularProgressIndicator() }
                        games.isEmpty() -> CenteredMessage {
                            Text(stringResource(R.string.no_games), style = MaterialTheme.typography.bodyLarge)
                        }
                        else -> GameList(
                            games = games,
                            gameDay = date,
                            followed = state.followed,
                            bottomPadding = padding.calculateBottomPadding(),
                            // Lets the app bar tint when the list scrolls under it.
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                            onGameClick = onGameClick,
                        )
                    }
                }
                state.hasError -> LoadError(onRetry)
                else -> CenteredMessage { CircularProgressIndicator() }
            }
        }
    }
}

/** The pager starts on the day the view model picks; after that, a swipe or a tab tap selects the day it settles on. */
@Composable
private fun SyncPagerWithSelection(
    pagerState: PagerState,
    days: List<LocalDate>,
    selectedDate: LocalDate?,
    onSelectDate: (LocalDate) -> Unit,
) {
    LaunchedEffect(days, selectedDate) {
        val index = days.indexOf(selectedDate)
        if (index >= 0 && index != pagerState.currentPage) pagerState.scrollToPage(index)
    }

    val currentDays by rememberUpdatedState(days)
    val currentOnSelectDate by rememberUpdatedState(onSelectDate)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            currentDays.getOrNull(page)?.let(currentOnSelectDate)
        }
    }
}

@Composable
private fun GameList(
    games: List<Game>,
    gameDay: LocalDate,
    followed: FollowedPlayers,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
    onGameClick: (gameId: Long) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottomPadding + 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(games, key = { it.id }) { game ->
            GameCard(
                game = game,
                gameDay = gameDay,
                followed = followed,
                onClick = { onGameClick(game.id) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayTabs(
    days: List<LocalDate>,
    gameCounts: Map<LocalDate, Int>,
    selectedIndex: Int,
    today: LocalDate,
    onSelect: (index: Int) -> Unit,
) {
    PrimaryScrollableTabRow(
        selectedTabIndex = selectedIndex.coerceIn(days.indices),
        containerColor = Color.Transparent,
        edgePadding = 16.dp,
    ) {
        days.forEachIndexed { index, date ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(dayLabel(date, today))
                        // Blank until that day's week has loaded; the empty line keeps the tab height steady.
                        val count = gameCounts[date]
                        Text(
                            if (count != null) pluralStringResource(R.plurals.game_count, count, count) else "",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                },
            )
        }
    }
}

/** Same easing Material uses for the app bar's own scrolled colour. */
@Composable
private fun headerColor(scrolledFraction: Float): Color = lerp(
    MaterialTheme.colorScheme.surface,
    MaterialTheme.colorScheme.surfaceContainer,
    FastOutLinearInEasing.transform(scrolledFraction),
)

@Composable
private fun dayLabel(date: LocalDate, today: LocalDate): String = when (date) {
    today -> stringResource(R.string.today)
    today.minusDays(1) -> stringResource(R.string.yesterday)
    today.plusDays(1) -> stringResource(R.string.tomorrow)
    else -> date.format(DateTimeFormatter.ofPattern("EEE d"))
}
