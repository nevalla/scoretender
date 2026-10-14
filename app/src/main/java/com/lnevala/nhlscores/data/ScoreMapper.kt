package com.lnevala.nhlscores.data

import com.lnevala.nhlscores.domain.Ending
import com.lnevala.nhlscores.domain.Game
import com.lnevala.nhlscores.domain.GameDay
import com.lnevala.nhlscores.domain.GameStatus
import com.lnevala.nhlscores.domain.PlayerPoints
import com.lnevala.nhlscores.domain.ScoreDay
import com.lnevala.nhlscores.domain.TeamLine
import java.time.Instant
import java.time.LocalDate

// The only place that knows the shape of the NHL's score endpoints.

fun ScoreResponseDto.toDomain() = ScoreDay(
    date = LocalDate.parse(currentDate),
    week = gameWeek.map { GameDay(LocalDate.parse(it.date), it.numberOfGames) },
    games = games.map { it.toDomain() },
)

fun GameDto.toDomain() = Game(
    id = id,
    startTime = Instant.parse(startTimeUTC),
    away = awayTeam.toDomain(pointsFor(awayTeam.abbrev)),
    home = homeTeam.toDomain(pointsFor(homeTeam.abbrev)),
    status = status(),
)

private fun TeamDto.toDomain(points: List<PlayerPoints>) = TeamLine(
    abbrev = abbrev,
    name = name.default,
    logoUrl = logo,
    darkLogoUrl = logo.replace("_light.svg", "_dark.svg"),
    score = score,
    record = record,
    points = points,
)

/** Shootout goals don't count as points. */
private fun GameDto.pointsFor(teamAbbrev: String): List<PlayerPoints> =
    goals
        .filter { it.teamAbbrev == teamAbbrev && it.periodDescriptor?.periodType != "SO" }
        .flatMap { goal ->
            listOf(PlayerPoints(goal.playerId, goal.name.default, goals = 1, assists = 0)) +
                goal.assists.map { PlayerPoints(it.playerId, it.name.default, goals = 0, assists = 1) }
        }
        .groupBy { it.playerId }
        .map { (_, entries) -> entries.reduce(PlayerPoints::plus) }
        .sortedWith(compareByDescending<PlayerPoints> { it.points }.thenByDescending { it.goals })

private fun GameDto.status() = gameStatus(gameState, gameScheduleState, clock, periodDescriptor, gameOutcome)

/** Shared by every endpoint that describes a game's state the same way. */
internal fun gameStatus(
    gameState: String,
    gameScheduleState: String,
    clock: ClockDto?,
    periodDescriptor: PeriodDescriptorDto?,
    gameOutcome: GameOutcomeDto?,
): GameStatus = when {
    gameScheduleState in setOf("PPD", "CNCL", "SUSP") -> GameStatus.Postponed
    gameState in setOf("LIVE", "CRIT") -> GameStatus.Live(
        period = periodDescriptor?.label().orEmpty(),
        clock = clock?.timeRemaining.orEmpty(),
        inIntermission = clock?.inIntermission ?: false,
    )
    gameState in setOf("OFF", "FINAL") ->
        // Some endpoints leave out gameOutcome; the last period played tells the same.
        GameStatus.Final(ending(gameOutcome?.lastPeriodType ?: periodDescriptor?.periodType))
    else -> GameStatus.Scheduled
}

private fun ending(periodType: String?) = when (periodType) {
    "OT" -> Ending.Overtime
    "SO" -> Ending.Shootout
    else -> Ending.Regulation
}

internal fun PeriodDescriptorDto.label(): String = when (periodType) {
    "SO" -> "SO"
    "OT" -> (number - maxRegulationPeriods).let { if (it > 1) "${it}OT" else "OT" }
    else -> when (number) {
        1 -> "1st"
        2 -> "2nd"
        3 -> "3rd"
        else -> "${number}th"
    }
}
