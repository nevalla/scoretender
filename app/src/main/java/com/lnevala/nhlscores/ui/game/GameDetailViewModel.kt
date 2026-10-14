package com.lnevala.nhlscores.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lnevala.nhlscores.NhlScoresApp
import com.lnevala.nhlscores.domain.GameDetail
import com.lnevala.nhlscores.domain.GameDetailRepository
import com.lnevala.nhlscores.domain.FollowedPlayers
import com.lnevala.nhlscores.domain.FollowedPlayersRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameDetailUiState(
    val followed: FollowedPlayers,
    val detail: GameDetail? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
)

class GameDetailViewModel(
    private val gameId: Long,
    private val repository: GameDetailRepository,
    private val followedPlayers: FollowedPlayersRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameDetailUiState(followed = followedPlayers.followed.value))
    val uiState: StateFlow<GameDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            followedPlayers.followed.collect { followed -> _uiState.update { it.copy(followed = followed) } }
        }
        load()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        load()
    }

    fun retry() {
        _uiState.update { it.copy(isLoading = true) }
        load()
    }

    /** Reloads without a spinner, for keeping a live game up to date. */
    fun refreshQuietly() = load()

    private fun load() {
        // Outside loadJob, so live polls don't keep cancelling a slow first lookup.
        viewModelScope.launch { followedPlayers.ensureLoaded() }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                val detail = repository.gameDetail(gameId)
                _uiState.update {
                    it.copy(
                        detail = detail,
                        isLoading = false,
                        isRefreshing = false,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Keep showing what we have; only an empty screen becomes an error.
                _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
            }
        }
    }

    companion object {
        fun factory(gameId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NhlScoresApp
                GameDetailViewModel(gameId, app.gameDetailRepository, app.followedPlayersRepository)
            }
        }
    }
}
