package com.lnevala.nhlscores.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lnevala.nhlscores.R
import com.lnevala.nhlscores.domain.GameDetail
import com.lnevala.nhlscores.domain.GamePreview
import com.lnevala.nhlscores.domain.Leader
import com.lnevala.nhlscores.domain.LeaderCategory
import com.lnevala.nhlscores.domain.LeaderComparison

fun LazyListScope.previewSections(detail: GameDetail, preview: GamePreview) {
    val away = detail.game.away.abbrev
    val home = detail.game.home.abbrev

    if (detail.venue != null || preview.seasonSeries != null) item(key = "info") {
        DetailSection(stringResource(R.string.game_info)) {
            detail.venue?.let { InfoRow(stringResource(R.string.venue), it) }
            preview.seasonSeries?.let {
                InfoRow(stringResource(R.string.season_series), "$away ${it.awayWins} – ${it.homeWins} $home")
            }
        }
    }
    if (preview.leaders.isNotEmpty()) item(key = "leaders") {
        DetailSection(stringResource(R.string.last_five_games)) {
            preview.leaders.forEach { LeaderRow(it) }
        }
    }
    if (preview.goalies.isNotEmpty()) item(key = "seasonGoalies") {
        DetailSection(stringResource(R.string.goalies_this_season)) {
            preview.goalies.forEach {
                PlayerStatRow(
                    name = it.player.name,
                    details = listOfNotNull(it.teamAbbrev, it.record).joinToString(" · "),
                    value = formatSavePercentage(it.savePercentage),
                    valueCaption = stringResource(R.string.goals_against_average, formatGoalsAgainstAverage(it.goalsAgainstAverage)),
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.End)
    }
}

/** Away team's leader on the left, home team's on the right, like the header. */
@Composable
private fun LeaderRow(comparison: LeaderComparison) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(
                when (comparison.category) {
                    LeaderCategory.Points -> R.string.leader_points
                    LeaderCategory.Goals -> R.string.leader_goals
                    LeaderCategory.Assists -> R.string.leader_assists
                },
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            LeaderCell(comparison.away, alignEnd = false, modifier = Modifier.weight(1f))
            LeaderCell(comparison.home, alignEnd = true, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun LeaderCell(leader: Leader?, alignEnd: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, if (alignEnd) Alignment.End else Alignment.Start),
    ) {
        if (leader == null) return@Row
        if (!alignEnd) Headshot(leader.headshotUrl)
        Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
            Text(
                text = leader.player.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(leader.value.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        if (alignEnd) Headshot(leader.headshotUrl)
    }
}

@Composable
private fun Headshot(url: String?) {
    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    )
}
