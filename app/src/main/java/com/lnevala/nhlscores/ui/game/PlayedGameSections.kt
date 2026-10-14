package com.lnevala.nhlscores.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lnevala.nhlscores.R
import com.lnevala.nhlscores.domain.FollowedPlayers
import com.lnevala.nhlscores.domain.GameDetail
import com.lnevala.nhlscores.domain.GoalEvent
import com.lnevala.nhlscores.domain.GoalSituation
import com.lnevala.nhlscores.domain.SkaterLine
import com.lnevala.nhlscores.domain.Star
import com.lnevala.nhlscores.domain.TeamStat
import com.lnevala.nhlscores.domain.TeamStatKind
import com.lnevala.nhlscores.ui.common.displayName
import com.lnevala.nhlscores.ui.common.flag
import com.lnevala.nhlscores.ui.common.logoForTheme

fun LazyListScope.playedGameSections(detail: GameDetail, followed: FollowedPlayers) {
    // Scorers first; big countries can have a dozen players in one game.
    val followedSkaters = detail.skaters.filter { it.player.id in followed }.sortedByDescending { it.goals + it.assists }

    val country = followed.country
    if (country != null && followedSkaters.isNotEmpty()) item(key = "followed") {
        DetailSection("${country.flag} ${country.displayName}") {
            followedSkaters.forEach { FollowedSkaterRow(it) }
        }
    }
    if (detail.goals.isNotEmpty()) item(key = "scoring") {
        DetailSection(stringResource(R.string.scoring)) { ScoringSummary(detail, followed) }
    }
    if (detail.teamStats.isNotEmpty()) item(key = "teamStats") {
        DetailSection(stringResource(R.string.team_stats)) { TeamStats(detail) }
    }
    if (detail.goalies.isNotEmpty()) item(key = "goalies") {
        DetailSection(stringResource(R.string.goalies)) {
            detail.goalies.forEach {
                PlayerStatRow(
                    name = playerName(it.player, followed),
                    details = listOfNotNull(it.teamAbbrev, it.decision, stringResource(R.string.time_on_ice, it.timeOnIce))
                        .joinToString(" · "),
                    value = formatSavePercentage(it.savePercentage),
                    valueCaption = stringResource(R.string.saves_of_shots, it.saves, it.shotsAgainst),
                )
            }
        }
    }
    if (detail.threeStars.isNotEmpty()) item(key = "threeStars") {
        DetailSection(stringResource(R.string.three_stars)) {
            detail.threeStars.forEach { ThreeStarRow(it, followed) }
        }
    }
}

@Composable
private fun FollowedSkaterRow(skater: SkaterLine) {
    PlayerStatRow(
        name = skater.player.name,
        details = stringResource(
            R.string.skater_stats,
            skater.teamAbbrev,
            formatPlusMinus(skater.plusMinus),
            skater.shots,
            skater.timeOnIce,
        ),
        value = "${skater.goals}+${skater.assists}",
        emphasized = skater.goals + skater.assists > 0,
    )
}

@Composable
private fun ScoringSummary(detail: GameDetail, followed: FollowedPlayers) {
    val logos = mapOf(
        detail.game.away.abbrev to detail.game.away.logoForTheme(),
        detail.game.home.abbrev to detail.game.home.logoForTheme(),
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        detail.goals.groupBy { it.period }.forEach { (period, goals) ->
            Text(
                text = period,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp),
            )
            goals.forEach { GoalRow(it, logos[it.teamAbbrev], followed) }
        }
    }
}

@Composable
private fun GoalRow(goal: GoalEvent, logoUrl: String?, followed: FollowedPlayers) {
    val uriHandler = LocalUriHandler.current
    val watchLabel = stringResource(R.string.watch_goal)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .then(
                if (goal.videoUrl != null) {
                    Modifier.clickable(onClickLabel = watchLabel) { uriHandler.openUri(goal.videoUrl) }
                } else {
                    Modifier
                },
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(model = logoUrl, contentDescription = goal.teamAbbrev, modifier = Modifier.size(32.dp))
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(1f),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = playerName(goal.scorer, followed),
                    style = MaterialTheme.typography.bodyLarge,
                )
                goal.situation?.let { SituationBadge(it) }
            }
            Text(
                text = goal.assists
                    .joinToString { playerName(it, followed) }
                    .ifEmpty { stringResource(R.string.unassisted) },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${goal.awayScore}–${goal.homeScore}", style = MaterialTheme.typography.titleSmall)
            Text(goal.time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (goal.videoUrl != null) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun SituationBadge(situation: GoalSituation) {
    Text(
        text = stringResource(
            when (situation) {
                GoalSituation.PowerPlay -> R.string.situation_power_play
                GoalSituation.ShortHanded -> R.string.situation_short_handed
                GoalSituation.EmptyNet -> R.string.situation_empty_net
                GoalSituation.PenaltyShot -> R.string.situation_penalty_shot
            },
        ),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier
            .padding(start = 8.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
private fun TeamStats(detail: GameDetail) {
    val (awayColor, homeColor) = teamColors()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row {
            Text(detail.game.away.abbrev, style = MaterialTheme.typography.labelLarge, color = awayColor)
            Spacer(Modifier.weight(1f))
            Text(detail.game.home.abbrev, style = MaterialTheme.typography.labelLarge, color = homeColor)
        }
        detail.teamStats.forEach { TeamStatRow(it, awayColor, homeColor) }
    }
}

@Composable
private fun TeamStatRow(stat: TeamStat, awayColor: Color, homeColor: Color) {
    val total = stat.awayValue + stat.homeValue
    val awayShare = if (total > 0) (stat.awayValue / total).toFloat() else 0.5f

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stat.awayLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = stringResource(stat.kind.label),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(stat.homeLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        Row(Modifier.height(6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (awayShare > 0f) {
                Box(Modifier.weight(awayShare).height(6.dp).background(awayColor, CircleShape))
            }
            if (awayShare < 1f) {
                Box(Modifier.weight(1f - awayShare).height(6.dp).background(homeColor, CircleShape))
            }
        }
    }
}

private val TeamStatKind.label
    get() = when (this) {
        TeamStatKind.Shots -> R.string.stat_shots
        TeamStatKind.FaceoffPercentage -> R.string.stat_faceoffs
        TeamStatKind.PowerPlay -> R.string.stat_power_play
        TeamStatKind.PenaltyMinutes -> R.string.stat_penalty_minutes
        TeamStatKind.Hits -> R.string.stat_hits
        TeamStatKind.BlockedShots -> R.string.stat_blocked_shots
        TeamStatKind.Giveaways -> R.string.stat_giveaways
        TeamStatKind.Takeaways -> R.string.stat_takeaways
    }

@Composable
private fun ThreeStarRow(star: Star, followed: FollowedPlayers) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = star.headshotUrl,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(1f),
        ) {
            Text(
                text = stringResource(
                    when (star.rank) {
                        1 -> R.string.first_star
                        2 -> R.string.second_star
                        else -> R.string.third_star
                    },
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(playerName(star.player, followed), style = MaterialTheme.typography.bodyLarge)
            Text(star.teamAbbrev, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = star.savePercentage?.let(::formatSavePercentage) ?: "${star.goals}+${star.assists}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.End,
        )
    }
}
