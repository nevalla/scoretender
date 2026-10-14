package com.lnevala.nhlscores.data

import com.lnevala.nhlscores.domain.Game
import com.lnevala.nhlscores.domain.GameDetail
import com.lnevala.nhlscores.domain.GamePreview
import com.lnevala.nhlscores.domain.GameStatus
import com.lnevala.nhlscores.domain.GoalEvent
import com.lnevala.nhlscores.domain.GoalSituation
import com.lnevala.nhlscores.domain.GoalieLine
import com.lnevala.nhlscores.domain.GoalieSeason
import com.lnevala.nhlscores.domain.Leader
import com.lnevala.nhlscores.domain.LeaderCategory
import com.lnevala.nhlscores.domain.LeaderComparison
import com.lnevala.nhlscores.domain.PeriodScore
import com.lnevala.nhlscores.domain.PlayerRef
import com.lnevala.nhlscores.domain.SeasonSeries
import com.lnevala.nhlscores.domain.SkaterLine
import com.lnevala.nhlscores.domain.Star
import com.lnevala.nhlscores.domain.TeamLine
import com.lnevala.nhlscores.domain.TeamStat
import com.lnevala.nhlscores.domain.TeamStatKind
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import java.time.Instant
import java.time.LocalDate
import kotlin.math.roundToInt

// The only place that knows the shape of the NHL's game center endpoints.

fun gameDetail(landing: LandingDto, rightRail: RightRailDto, boxscore: BoxscoreDto): GameDetail {
    val status = gameStatus(
        landing.gameState,
        landing.gameScheduleState,
        landing.clock,
        landing.periodDescriptor,
        landing.gameOutcome,
    )
    val away = landing.awayTeam.abbrev
    val home = landing.homeTeam.abbrev
    val players = boxscore.playerByGameStats

    return GameDetail(
        game = Game(
            id = landing.id,
            startTime = Instant.parse(landing.startTimeUTC),
            away = landing.awayTeam.toDomain(),
            home = landing.homeTeam.toDomain(),
            status = status,
        ),
        gameDay = LocalDate.parse(landing.gameDate),
        venue = landing.venue?.default,
        periods = rightRail.linescore?.byPeriod.orEmpty().map {
            PeriodScore(it.periodDescriptor.label(), it.away, it.home)
        },
        goals = landing.summary?.scoring.orEmpty().flatMap { period ->
            period.goals.map { it.toDomain(period.periodDescriptor.label()) }
        },
        teamStats = rightRail.teamGameStats.mapNotNull { it.toDomain() },
        goalies = players?.let {
            it.awayTeam.goalies.map { g -> g.toDomain(away) } + it.homeTeam.goalies.map { g -> g.toDomain(home) }
        }.orEmpty().filter { it.timeOnIce != "00:00" },
        skaters = players?.let {
            (it.awayTeam.forwards + it.awayTeam.defense).map { s -> s.toDomain(away) } +
                (it.homeTeam.forwards + it.homeTeam.defense).map { s -> s.toDomain(home) }
        }.orEmpty(),
        threeStars = landing.summary?.threeStars.orEmpty().map { it.toDomain() }.sortedBy { it.rank },
        preview = if (status == GameStatus.Scheduled) preview(landing, rightRail) else null,
    )
}

private fun LandingTeamDto.toDomain() = TeamLine(
    abbrev = abbrev,
    name = commonName.default,
    logoUrl = logo,
    darkLogoUrl = darkLogo ?: logo.replace("_light.svg", "_dark.svg"),
    score = score,
    record = null,
    points = emptyList(),
)

private fun LandingGoalDto.toDomain(period: String) = GoalEvent(
    period = period,
    time = timeInPeriod,
    teamAbbrev = teamAbbrev.default,
    scorer = PlayerRef(playerId, name.default),
    assists = assists.map { PlayerRef(it.playerId, it.name.default) },
    situation = when {
        goalModifier == "empty-net" -> GoalSituation.EmptyNet
        goalModifier == "penalty-shot" -> GoalSituation.PenaltyShot
        strength == "pp" -> GoalSituation.PowerPlay
        strength == "sh" -> GoalSituation.ShortHanded
        else -> null
    },
    awayScore = awayScore,
    homeScore = homeScore,
    videoUrl = highlightClipSharingUrl,
)

/** The categories we show, with power play ("1/3") and faceoff share turned into bar values; null for the rest. */
private fun TeamGameStatDto.toDomain(): TeamStat? {
    fun plain(kind: TeamStatKind) = TeamStat(kind, awayValue.content, homeValue.content, awayValue.number, homeValue.number)

    return when (category) {
        "sog" -> plain(TeamStatKind.Shots)
        "faceoffWinningPctg" -> TeamStat(
            TeamStatKind.FaceoffPercentage,
            awayValue.percentage,
            homeValue.percentage,
            awayValue.number,
            homeValue.number,
        )
        "powerPlay" -> TeamStat(
            TeamStatKind.PowerPlay,
            awayValue.content,
            homeValue.content,
            awayValue.content.substringBefore("/").toDoubleOrNull() ?: 0.0,
            homeValue.content.substringBefore("/").toDoubleOrNull() ?: 0.0,
        )
        "pim" -> plain(TeamStatKind.PenaltyMinutes)
        "hits" -> plain(TeamStatKind.Hits)
        "blockedShots" -> plain(TeamStatKind.BlockedShots)
        "giveaways" -> plain(TeamStatKind.Giveaways)
        "takeaways" -> plain(TeamStatKind.Takeaways)
        else -> null
    }
}

private val JsonPrimitive.number get() = doubleOrNull ?: 0.0

private val JsonPrimitive.percentage get() = "${(number * 100).roundToInt()}%"

private fun GoalieStatsDto.toDomain(teamAbbrev: String) = GoalieLine(
    player = PlayerRef(playerId, name.default),
    teamAbbrev = teamAbbrev,
    saves = saves,
    shotsAgainst = shotsAgainst,
    savePercentage = savePctg,
    timeOnIce = toi,
    decision = decision,
)

private fun SkaterStatsDto.toDomain(teamAbbrev: String) = SkaterLine(
    player = PlayerRef(playerId, name.default),
    teamAbbrev = teamAbbrev,
    goals = goals,
    assists = assists,
    plusMinus = plusMinus,
    shots = sog,
    hits = hits,
    timeOnIce = toi,
)

private fun StarDto.toDomain() = Star(
    rank = star,
    player = PlayerRef(playerId, name.default),
    teamAbbrev = teamAbbrev,
    headshotUrl = headshot,
    goals = goals,
    assists = assists,
    savePercentage = if (position == "G") savePctg else null,
)

private fun preview(landing: LandingDto, rightRail: RightRailDto): GamePreview {
    val matchup = landing.matchup
    val goalies = matchup?.goalieComparison
    return GamePreview(
        leaders = matchup?.skaterComparison?.leaders.orEmpty().mapNotNull { pair ->
            val category = when (pair.category) {
                "points" -> LeaderCategory.Points
                "goals" -> LeaderCategory.Goals
                "assists" -> LeaderCategory.Assists
                else -> return@mapNotNull null
            }
            LeaderComparison(category, pair.awayLeader?.toDomain(), pair.homeLeader?.toDomain())
        },
        goalies = goalies?.awayTeam?.leaders.orEmpty().map { it.toDomain(landing.awayTeam.abbrev) } +
            goalies?.homeTeam?.leaders.orEmpty().map { it.toDomain(landing.homeTeam.abbrev) },
        seasonSeries = rightRail.seasonSeriesWins?.let { SeasonSeries(it.awayTeamWins, it.homeTeamWins) },
    )
}

private fun LeaderDto.toDomain() = Leader(PlayerRef(playerId, name.default), headshot, value.roundToInt())

private fun GoalieLeaderDto.toDomain(teamAbbrev: String) = GoalieSeason(
    player = PlayerRef(playerId, name.default),
    teamAbbrev = teamAbbrev,
    record = record,
    goalsAgainstAverage = gaa,
    savePercentage = savePctg,
)
