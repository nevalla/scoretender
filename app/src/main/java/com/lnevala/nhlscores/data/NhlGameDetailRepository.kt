package com.lnevala.nhlscores.data

import com.lnevala.nhlscores.domain.GameDetail
import com.lnevala.nhlscores.domain.GameDetailRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class NhlGameDetailRepository(private val api: NhlApi) : GameDetailRepository {
    override suspend fun gameDetail(gameId: Long): GameDetail = coroutineScope {
        val landing = async { api.landing(gameId) }
        val rightRail = async { api.rightRail(gameId) }
        val boxscore = async { api.boxscore(gameId) }
        gameDetail(landing.await(), rightRail.await(), boxscore.await())
    }
}
