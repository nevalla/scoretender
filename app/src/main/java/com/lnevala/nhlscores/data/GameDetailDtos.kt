package com.lnevala.nhlscores.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

// gamecenter/{id}/landing

@Serializable
data class LandingDto(
    val id: Long,
    val gameDate: String,
    val startTimeUTC: String,
    val gameState: String,
    val gameScheduleState: String = "OK",
    val venue: LocalizedDto? = null,
    val awayTeam: LandingTeamDto,
    val homeTeam: LandingTeamDto,
    val clock: ClockDto? = null,
    val periodDescriptor: PeriodDescriptorDto? = null,
    val gameOutcome: GameOutcomeDto? = null,
    val summary: SummaryDto? = null,
    val matchup: MatchupDto? = null,
)

@Serializable
data class LandingTeamDto(
    val abbrev: String,
    val commonName: LocalizedDto,
    val logo: String,
    val darkLogo: String? = null,
    val score: Int? = null,
)

@Serializable
data class SummaryDto(
    val scoring: List<ScoringPeriodDto> = emptyList(),
    val threeStars: List<StarDto> = emptyList(),
)

@Serializable
data class ScoringPeriodDto(val periodDescriptor: PeriodDescriptorDto, val goals: List<LandingGoalDto> = emptyList())

@Serializable
data class LandingGoalDto(
    val playerId: Long,
    val name: LocalizedDto,
    val teamAbbrev: LocalizedDto,
    val timeInPeriod: String,
    val strength: String = "ev",
    val goalModifier: String = "none",
    val awayScore: Int,
    val homeScore: Int,
    val assists: List<LandingAssistDto> = emptyList(),
    val highlightClipSharingUrl: String? = null,
)

@Serializable
data class LandingAssistDto(val playerId: Long, val name: LocalizedDto)

@Serializable
data class StarDto(
    val star: Int,
    val playerId: Long,
    val teamAbbrev: String,
    val name: LocalizedDto,
    val headshot: String? = null,
    val position: String? = null,
    val goals: Int = 0,
    val assists: Int = 0,
    val savePctg: Double? = null,
)

@Serializable
data class MatchupDto(
    val skaterComparison: SkaterComparisonDto? = null,
    val goalieComparison: GoalieComparisonDto? = null,
)

@Serializable
data class SkaterComparisonDto(val leaders: List<LeaderPairDto> = emptyList())

@Serializable
data class LeaderPairDto(val category: String, val awayLeader: LeaderDto? = null, val homeLeader: LeaderDto? = null)

@Serializable
data class LeaderDto(val playerId: Long, val name: LocalizedDto, val headshot: String? = null, val value: Double)

@Serializable
data class GoalieComparisonDto(val awayTeam: GoalieTeamDto? = null, val homeTeam: GoalieTeamDto? = null)

@Serializable
data class GoalieTeamDto(val leaders: List<GoalieLeaderDto> = emptyList())

@Serializable
data class GoalieLeaderDto(
    val playerId: Long,
    val name: LocalizedDto,
    val record: String? = null,
    val gaa: Double? = null,
    val savePctg: Double? = null,
)

// gamecenter/{id}/right-rail

@Serializable
data class RightRailDto(
    val linescore: LinescoreDto? = null,
    val teamGameStats: List<TeamGameStatDto> = emptyList(),
    val seasonSeriesWins: SeasonSeriesWinsDto? = null,
)

@Serializable
data class LinescoreDto(val byPeriod: List<LinescorePeriodDto> = emptyList())

@Serializable
data class LinescorePeriodDto(val periodDescriptor: PeriodDescriptorDto, val away: Int, val home: Int)

/** Values are numbers, fractions or strings like "1/3" depending on the category. */
@Serializable
data class TeamGameStatDto(val category: String, val awayValue: JsonPrimitive, val homeValue: JsonPrimitive)

@Serializable
data class SeasonSeriesWinsDto(val awayTeamWins: Int = 0, val homeTeamWins: Int = 0)

// gamecenter/{id}/boxscore

@Serializable
data class BoxscoreDto(val playerByGameStats: PlayerByGameStatsDto? = null)

@Serializable
data class PlayerByGameStatsDto(val awayTeam: TeamPlayerStatsDto, val homeTeam: TeamPlayerStatsDto)

@Serializable
data class TeamPlayerStatsDto(
    val forwards: List<SkaterStatsDto> = emptyList(),
    val defense: List<SkaterStatsDto> = emptyList(),
    val goalies: List<GoalieStatsDto> = emptyList(),
)

@Serializable
data class SkaterStatsDto(
    val playerId: Long,
    val name: LocalizedDto,
    val goals: Int = 0,
    val assists: Int = 0,
    val plusMinus: Int = 0,
    val sog: Int = 0,
    val hits: Int = 0,
    val toi: String = "00:00",
)

@Serializable
data class GoalieStatsDto(
    val playerId: Long,
    val name: LocalizedDto,
    val saves: Int = 0,
    val shotsAgainst: Int = 0,
    val savePctg: Double? = null,
    val toi: String = "00:00",
    val decision: String? = null,
)
