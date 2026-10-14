package com.lnevala.nhlscores.data

import com.lnevala.nhlscores.domain.PlayerNationalityRepository
import kotlinx.coroutines.async
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
        val skaters = async { api.bios("skater", filter) }
        val goalies = async { api.bios("goalie", filter) }
        (skaters.await().data + goalies.await().data).mapTo(HashSet()) { it.playerId }
    }
}
