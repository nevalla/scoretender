package com.lnevala.nhlscores.data

import com.lnevala.nhlscores.domain.PlayerNationalityRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Nationalities hardly change, so each list is fetched once per app session. */
class NhlPlayerNationalityRepository(private val api: NhlStatsApi) : PlayerNationalityRepository {

    private val mutex = Mutex()
    private val cache = mutableMapOf<String, Set<Long>>()

    override suspend fun playerIds(nationalityCode: String): Set<Long> = mutex.withLock {
        cache[nationalityCode] ?: fetch(nationalityCode).also { cache[nationalityCode] = it }
    }

    private suspend fun fetch(nationalityCode: String): Set<Long> = coroutineScope {
        val filter = "nationalityCode=\"$nationalityCode\""
        val skaters = async { allBios("skater", filter) }
        val goalies = async { allBios("goalie", filter) }
        (skaters.await() + goalies.await()).mapTo(HashSet()) { it.playerId }
    }

    /**
     * The first page tells how many players there are and how many the API gives at once,
     * which may be fewer than asked for; the remaining pages load in parallel.
     */
    private suspend fun allBios(position: String, filter: String): List<PlayerBioDto> = coroutineScope {
        val first = api.bios(position, filter)
        val pageSize = first.data.size
        if (pageSize == 0) return@coroutineScope first.data
        val rest = (pageSize until first.total step pageSize)
            .map { start -> async { api.bios(position, filter, start).data } }
        first.data + rest.awaitAll().flatten()
    }
}
