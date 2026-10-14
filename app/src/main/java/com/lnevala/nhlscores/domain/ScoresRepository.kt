package com.lnevala.nhlscores.domain

import java.time.LocalDate

interface ScoresRepository {
    /** Scores for the league's current game day, which follows North American time. */
    suspend fun currentScores(): ScoreDay

    suspend fun scores(date: LocalDate): ScoreDay
}
