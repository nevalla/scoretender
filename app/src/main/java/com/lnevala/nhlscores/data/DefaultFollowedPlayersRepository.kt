package com.lnevala.nhlscores.data

import com.lnevala.nhlscores.domain.Country
import com.lnevala.nhlscores.domain.CountryStore
import com.lnevala.nhlscores.domain.FollowedPlayers
import com.lnevala.nhlscores.domain.FollowedPlayersRepository
import com.lnevala.nhlscores.domain.PlayerNationalityRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DefaultFollowedPlayersRepository(
    private val store: CountryStore,
    private val nationalities: PlayerNationalityRepository,
    private val scope: CoroutineScope,
) : FollowedPlayersRepository {

    private val _followed = MutableStateFlow(FollowedPlayers(store.load()))
    override val followed: StateFlow<FollowedPlayers> = _followed.asStateFlow()

    override fun follow(country: Country?) {
        if (country == _followed.value.country) return
        store.save(country)
        _followed.value = FollowedPlayers(country)
        scope.launch { ensureLoaded() }
    }

    override suspend fun ensureLoaded() {
        val country = _followed.value.country ?: return
        if (_followed.value.playerIds.isNotEmpty()) return
        val ids = try {
            nationalities.playerIds(country.code)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Screens work without highlights; their next load tries again.
            return
        }
        // The user may have picked another country while this one was loading.
        _followed.update { if (it.country == country) it.copy(playerIds = ids) else it }
    }
}
