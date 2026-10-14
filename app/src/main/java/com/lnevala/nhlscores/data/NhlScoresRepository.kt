package com.lnevala.nhlscores.data

import com.lnevala.nhlscores.domain.ScoreDay
import com.lnevala.nhlscores.domain.ScoresRepository
import java.time.LocalDate

class NhlScoresRepository(private val api: NhlApi) : ScoresRepository {
    override suspend fun currentScores(): ScoreDay = api.currentScore().toDomain()

    override suspend fun scores(date: LocalDate): ScoreDay = api.score(date.toString()).toDomain()
}
