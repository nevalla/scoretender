package com.lnevala.nhlscores.data

import kotlinx.serialization.Serializable

@Serializable
data class ScoreResponseDto(
    val currentDate: String,
    val gameWeek: List<GameWeekDayDto> = emptyList(),
    val games: List<GameDto> = emptyList(),
)

@Serializable
data class GameWeekDayDto(val date: String, val numberOfGames: Int)

@Serializable
data class GameDto(
    val id: Long,
    val startTimeUTC: String,
    val gameState: String,
    val gameScheduleState: String = "OK",
    val awayTeam: TeamDto,
    val homeTeam: TeamDto,
    val clock: ClockDto? = null,
    val periodDescriptor: PeriodDescriptorDto? = null,
    val gameOutcome: GameOutcomeDto? = null,
    val goals: List<GoalDto> = emptyList(),
)

@Serializable
data class GoalDto(
    val playerId: Long,
    val name: LocalizedDto,
    val teamAbbrev: String,
    val periodDescriptor: PeriodDescriptorDto? = null,
    val assists: List<AssistDto> = emptyList(),
)

@Serializable
data class AssistDto(val playerId: Long, val name: LocalizedDto)

@Serializable
data class TeamDto(
    val abbrev: String,
    val name: LocalizedDto,
    val logo: String,
    val score: Int? = null,
    val record: String? = null,
)

@Serializable
data class LocalizedDto(val default: String)

@Serializable
data class ClockDto(val timeRemaining: String, val inIntermission: Boolean = false)

@Serializable
data class PeriodDescriptorDto(val number: Int, val periodType: String, val maxRegulationPeriods: Int = 3)

@Serializable
data class GameOutcomeDto(val lastPeriodType: String)

@Serializable
data class PlayerBiosDto(val data: List<PlayerBioDto>)

@Serializable
data class PlayerBioDto(val playerId: Long)
