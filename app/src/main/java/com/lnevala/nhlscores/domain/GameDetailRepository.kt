package com.lnevala.nhlscores.domain

interface GameDetailRepository {
    suspend fun gameDetail(gameId: Long): GameDetail
}
