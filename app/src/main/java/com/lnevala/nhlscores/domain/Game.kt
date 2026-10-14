package com.lnevala.nhlscores.domain

import java.time.Instant
import java.time.LocalDate

data class ScoreDay(
    val date: LocalDate,
    val week: List<GameDay>,
    val games: List<Game>,
)

data class GameDay(val date: LocalDate, val gameCount: Int)

data class Game(
    val id: Long,
    val startTime: Instant,
    val away: TeamLine,
    val home: TeamLine,
    val status: GameStatus,
)

data class TeamLine(
    val abbrev: String,
    val name: String,
    val logoUrl: String,
    val darkLogoUrl: String,
    val score: Int?,
    val record: String?,
    /** Players with points in this game, best first. */
    val points: List<PlayerPoints>,
)

sealed interface GameStatus {
    data object Scheduled : GameStatus
    data object Postponed : GameStatus
    data class Live(val period: String, val clock: String, val inIntermission: Boolean) : GameStatus
    data class Final(val ending: Ending) : GameStatus
}

enum class Ending { Regulation, Overtime, Shootout }

val Game.isLive: Boolean get() = status is GameStatus.Live
