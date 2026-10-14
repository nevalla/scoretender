package com.lnevala.nhlscores.ui.scores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lnevala.nhlscores.NhlScoresApp
import com.lnevala.nhlscores.domain.Game
import com.lnevala.nhlscores.domain.GameDay
import com.lnevala.nhlscores.domain.Country
import com.lnevala.nhlscores.domain.FollowedPlayers
import com.lnevala.nhlscores.domain.FollowedPlayersRepository
import com.lnevala.nhlscores.domain.ScoreDay
import com.lnevala.nhlscores.domain.ScoresRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ScoresUiState(
    val followed: FollowedPlayers,
    /** The league's current game day; null until the first load finishes. */
    val today: LocalDate? = null,
    val selectedDate: LocalDate? = null,
    /** A fixed range around today, so tabs and pages stay put while moving between days. */
    val days: List<LocalDate> = emptyList(),
    /** Filled in from each loaded week, since the API only tells a week at a time. */
    val gameCounts: Map<LocalDate, Int> = emptyMap(),
    /** Days loaded so far, so swiping back and forth shows scores right away. */
    val gamesByDate: Map<LocalDate, List<Game>> = emptyMap(),
    val isRefreshing: Boolean = false,
    val hasError: Boolean = false,
) {
    val games: List<Game> get() = gamesByDate[selectedDate].orEmpty()
}

class ScoresViewModel(
    private val repository: ScoresRepository,
    private val followedPlayers: FollowedPlayersRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoresUiState(followed = followedPlayers.followed.value))
    val uiState: StateFlow<ScoresUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private val prefetchJobs = mutableMapOf<LocalDate, Job>()

    init {
        viewModelScope.launch {
            followedPlayers.followed.collect { followed -> _uiState.update { it.copy(followed = followed) } }
        }
        load { repository.currentScores() }
    }

    fun follow(country: Country?) = followedPlayers.follow(country)

    fun selectDate(date: LocalDate) {
        if (date == _uiState.value.selectedDate) return
        _uiState.update {
            it.copy(
                // The calendar can pick a day outside the tabs; the tabs then move to a range around it.
                days = if (date in it.days) it.days else daysAround(date),
                selectedDate = date,
                hasError = false,
            )
        }
        reload()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        reload()
    }

    fun retry() {
        _uiState.update { it.copy(hasError = false) }
        reload()
    }

    /** Reloads without a spinner, for keeping live games up to date. */
    fun refreshQuietly() = reload()

    private fun reload() {
        val date = _uiState.value.selectedDate
        load { if (date == null) repository.currentScores() else repository.scores(date) }
    }

    private fun load(fetch: suspend () -> ScoreDay) {
        // Outside loadJob, so swipes and live polls don't keep cancelling a slow first lookup.
        viewModelScope.launch { followedPlayers.ensureLoaded() }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                val day = fetch()
                _uiState.update {
                    it.copy(
                        today = it.today ?: day.date,
                        selectedDate = day.date,
                        days = it.days.ifEmpty { daysAround(day.date) },
                        gameCounts = it.gameCounts + day.week.gameCounts(),
                        gamesByDate = it.gamesByDate + (day.date to day.games),
                        isRefreshing = false,
                        hasError = false,
                    )
                }
                prefetchNeighbours(day)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Keep showing the last scores we have; only an empty screen becomes an error.
                _uiState.update { it.copy(isRefreshing = false, hasError = it.games.isEmpty()) }
            }
        }
    }

    /** Loads the days on either side in the background, so a swipe lands on scores, not a spinner. */
    private fun prefetchNeighbours(day: ScoreDay) {
        listOf(day.date.minusDays(1), day.date.plusDays(1))
            .filter { date -> date in _uiState.value.days && date !in _uiState.value.gamesByDate }
            .filter { prefetchJobs[it]?.isActive != true }
            .forEach { date ->
                prefetchJobs[date] = viewModelScope.launch {
                    try {
                        val neighbour = repository.scores(date)
                        _uiState.update {
                            it.copy(
                                gamesByDate = it.gamesByDate + (date to neighbour.games),
                                gameCounts = it.gameCounts + neighbour.week.gameCounts(),
                            )
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // Not needed yet; the day loads normally once it's selected.
                    }
                }
            }
    }

    private fun List<GameDay>.gameCounts() = associate { it.date to it.gameCount }

    private fun daysAround(today: LocalDate) =
        (-DAYS_EACH_WAY..DAYS_EACH_WAY).map { today.plusDays(it) }

    companion object {
        private const val DAYS_EACH_WAY = 30L

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NhlScoresApp
                ScoresViewModel(app.scoresRepository, app.followedPlayersRepository)
            }
        }
    }
}
