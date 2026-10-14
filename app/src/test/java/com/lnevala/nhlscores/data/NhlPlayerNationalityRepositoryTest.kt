package com.lnevala.nhlscores.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class NhlPlayerNationalityRepositoryTest {

    private class FakeStatsApi : NhlStatsApi {
        val requests = mutableListOf<Pair<String, String>>()
        var failing = false

        override suspend fun bios(position: String, filter: String, limit: Int): PlayerBiosDto {
            requests += position to filter
            if (failing) error("offline")
            val ids = if (position == "skater") listOf(1L, 2L, 2L) else listOf(3L)
            return PlayerBiosDto(ids.map(::PlayerBioDto))
        }
    }

    private val api = FakeStatsApi()
    private val repository = NhlPlayerNationalityRepository(api)

    @Test
    fun `combines skaters and goalies of the nationality`() = runBlocking {
        assertEquals(setOf(1L, 2L, 3L), repository.playerIds("FIN"))
        assertEquals(
            setOf("skater" to "nationalityCode=\"FIN\"", "goalie" to "nationalityCode=\"FIN\""),
            api.requests.toSet(),
        )
    }

    @Test
    fun `fetches each nationality only once`() = runBlocking {
        repository.playerIds("FIN")
        repository.playerIds("FIN")
        repository.playerIds("SWE")

        assertEquals(4, api.requests.size)
    }

    @Test
    fun `does not cache failures`() = runBlocking {
        api.failing = true
        assertThrows(IllegalStateException::class.java) { runBlocking { repository.playerIds("FIN") } }

        api.failing = false
        assertEquals(setOf(1L, 2L, 3L), repository.playerIds("FIN"))
    }
}
