package com.lnevala.nhlscores.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class NhlPlayerNationalityRepositoryTest {

    /** Hands out players a page at a time, like the real API. */
    private class FakeStatsApi : NhlStatsApi {
        val requests = mutableListOf<Pair<String, String>>()
        var failing = false
        var skaters = listOf(1L, 2L, 2L)
        var goalies = listOf(3L)
        var cap = NhlStatsApi.PAGE_SIZE

        override suspend fun bios(position: String, filter: String, start: Int, limit: Int, sort: String): PlayerBiosDto {
            requests += position to filter
            if (failing) error("offline")
            val ids = if (position == "skater") skaters else goalies
            val page = ids.drop(start).take(minOf(limit, cap))
            return PlayerBiosDto(page.map(::PlayerBioDto), total = ids.size)
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
    fun `fetches every page`() = runBlocking {
        api.skaters = (1L..250L).toList()

        assertEquals((1L..250L).toSet() + 3L, repository.playerIds("FIN"))
        assertEquals(3, api.requests.count { it.first == "skater" })
    }

    @Test
    fun `follows the page size the API gives`() = runBlocking {
        api.skaters = (1L..250L).toList()
        api.cap = 50

        assertEquals((1L..250L).toSet() + 3L, repository.playerIds("FIN"))
        assertEquals(5, api.requests.count { it.first == "skater" })
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
