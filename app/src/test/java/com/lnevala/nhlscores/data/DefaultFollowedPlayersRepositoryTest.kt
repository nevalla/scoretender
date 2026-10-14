package com.lnevala.nhlscores.data

import com.lnevala.nhlscores.domain.Country
import com.lnevala.nhlscores.domain.CountryStore
import com.lnevala.nhlscores.domain.FollowedPlayers
import com.lnevala.nhlscores.domain.PlayerNationalityRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultFollowedPlayersRepositoryTest {

    private val sweden = Country("SWE")

    private class FakeStore(var country: Country? = Country.Finland) : CountryStore {
        override fun load() = country
        override fun save(country: Country?) {
            this.country = country
        }
    }

    private class FakeNationalities : PlayerNationalityRepository {
        val ids = mapOf("FIN" to setOf(1L, 2L), "SWE" to setOf(3L))
        var failing = false
        var calls = 0
        val gates = mutableMapOf<String, CompletableDeferred<Unit>>()

        override suspend fun playerIds(nationalityCode: String): Set<Long> {
            calls++
            gates[nationalityCode]?.await()
            if (failing) error("offline")
            return ids.getValue(nationalityCode)
        }
    }

    private val store = FakeStore()
    private val nationalities = FakeNationalities()
    private val repository = DefaultFollowedPlayersRepository(store, nationalities, CoroutineScope(Dispatchers.Unconfined))

    @Test
    fun `starts with the saved country and loads its players`() = runBlocking {
        assertEquals(FollowedPlayers(Country.Finland), repository.followed.value)
        repository.ensureLoaded()
        assertEquals(FollowedPlayers(Country.Finland, setOf(1L, 2L)), repository.followed.value)
    }

    @Test
    fun `following another country saves it and loads its players`() {
        repository.follow(sweden)
        assertEquals(sweden, store.country)
        assertEquals(FollowedPlayers(sweden, setOf(3L)), repository.followed.value)
    }

    @Test
    fun `a failed load leaves no highlights and is tried again`() = runBlocking {
        nationalities.failing = true
        repository.ensureLoaded()
        assertEquals(FollowedPlayers(Country.Finland), repository.followed.value)

        nationalities.failing = false
        repository.ensureLoaded()
        assertEquals(setOf(1L, 2L), repository.followed.value.playerIds)
    }

    @Test
    fun `a slow load for the old country does not overwrite the new one`() = runBlocking {
        val finnishGate = CompletableDeferred<Unit>().also { nationalities.gates["FIN"] = it }
        val slowLoad = launch(Dispatchers.Unconfined) { repository.ensureLoaded() }

        repository.follow(sweden)
        finnishGate.complete(Unit)
        slowLoad.join()

        assertEquals(FollowedPlayers(sweden, setOf(3L)), repository.followed.value)
    }

    @Test
    fun `following no country saves that and loads no one`() = runBlocking {
        repository.follow(null)
        repository.ensureLoaded()

        assertEquals(null, store.country)
        assertEquals(FollowedPlayers(null), repository.followed.value)
        assertEquals(0, nationalities.calls)
    }
}
