package com.lnevala.nhlscores.domain

import java.time.LocalDate

data class GameDetail(
    val game: Game,
    val gameDay: LocalDate,
    val venue: String?,
    val periods: List<PeriodScore>,
    val goals: List<GoalEvent>,
    val teamStats: List<TeamStat>,
    val goalies: List<GoalieLine>,
    val skaters: List<SkaterLine>,
    val threeStars: List<Star>,
    /** Only for games that haven't started. */
    val preview: GamePreview?,
)

data class PeriodScore(val period: String, val away: Int, val home: Int)

/** [name] is the first initial and last name, e.g. "A. Protas", which tells brothers apart. */
data class PlayerRef(val id: Long, val name: String)

data class GoalEvent(
    val period: String,
    val time: String,
    val teamAbbrev: String,
    val scorer: PlayerRef,
    val assists: List<PlayerRef>,
    val situation: GoalSituation?,
    val awayScore: Int,
    val homeScore: Int,
    val videoUrl: String?,
)

enum class GoalSituation { PowerPlay, ShortHanded, EmptyNet, PenaltyShot }

enum class TeamStatKind { Shots, FaceoffPercentage, PowerPlay, PenaltyMinutes, Hits, BlockedShots, Giveaways, Takeaways }

/** [awayLabel] and [homeLabel] are for display; the values size the comparison bar. */
data class TeamStat(
    val kind: TeamStatKind,
    val awayLabel: String,
    val homeLabel: String,
    val awayValue: Double,
    val homeValue: Double,
)

data class SkaterLine(
    val player: PlayerRef,
    val teamAbbrev: String,
    val goals: Int,
    val assists: Int,
    val plusMinus: Int,
    val shots: Int,
    val hits: Int,
    val timeOnIce: String,
)

data class GoalieLine(
    val player: PlayerRef,
    val teamAbbrev: String,
    val saves: Int,
    val shotsAgainst: Int,
    val savePercentage: Double?,
    val timeOnIce: String,
    /** "W", "L" or "O" once the game is decided. */
    val decision: String?,
)

data class Star(
    val rank: Int,
    val player: PlayerRef,
    val teamAbbrev: String,
    val headshotUrl: String?,
    val goals: Int,
    val assists: Int,
    /** Set for goalies instead of points. */
    val savePercentage: Double?,
)

data class GamePreview(
    val leaders: List<LeaderComparison>,
    val goalies: List<GoalieSeason>,
    val seasonSeries: SeasonSeries?,
)

enum class LeaderCategory { Points, Goals, Assists }

/** Each team's best player in a category over the last five games. */
data class LeaderComparison(val category: LeaderCategory, val away: Leader?, val home: Leader?)

data class Leader(val player: PlayerRef, val headshotUrl: String?, val value: Int)

data class GoalieSeason(
    val player: PlayerRef,
    val teamAbbrev: String,
    val record: String?,
    val goalsAgainstAverage: Double?,
    val savePercentage: Double?,
)

data class SeasonSeries(val awayWins: Int, val homeWins: Int)
