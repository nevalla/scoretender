package com.lnevala.nhlscores.ui.scores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lnevala.nhlscores.R
import com.lnevala.nhlscores.domain.Country
import com.lnevala.nhlscores.domain.Ending
import com.lnevala.nhlscores.domain.FollowedPlayers
import com.lnevala.nhlscores.domain.Game
import com.lnevala.nhlscores.domain.GameStatus
import com.lnevala.nhlscores.domain.PlayerPoints
import com.lnevala.nhlscores.domain.TeamLine
import com.lnevala.nhlscores.ui.common.gameContainer
import com.lnevala.nhlscores.ui.common.logoForTheme
import com.lnevala.nhlscores.ui.theme.NhlScoresTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun GameCard(
    game: Game,
    gameDay: LocalDate,
    followed: FollowedPlayers,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.gameContainer(game.id, CardDefaults.shape),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TeamRow(game.away, game.status, followed, isTrailing = game.isTrailing(game.away))
                TeamRow(game.home, game.status, followed, isTrailing = game.isTrailing(game.home))
            }
            VerticalDivider(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .height(64.dp),
            )
            StatusLabel(game, gameDay, Modifier.width(72.dp))
        }
    }
}

@Composable
private fun TeamRow(team: TeamLine, status: GameStatus, followed: FollowedPlayers, isTrailing: Boolean) {
    val textColor =
        if (isTrailing) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface

    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = team.logoForTheme(),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f),
        ) {
            Text(team.name, style = MaterialTheme.typography.titleMedium, color = textColor)
            when (status) {
                GameStatus.Scheduled, GameStatus.Postponed -> team.record?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> team.highlightedPlayers(followed).takeIf { it.isNotEmpty() }?.let {
                    Text(
                        text = team.highlightsText(it, followed),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        team.score?.let {
            Text(
                text = it.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = if (isTrailing) FontWeight.Normal else FontWeight.SemiBold,
                color = textColor,
            )
        }
    }
}

@Composable
internal fun StatusLabel(game: Game, gameDay: LocalDate, modifier: Modifier = Modifier) {
    val status = game.status
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        when (status) {
            GameStatus.Scheduled -> {
                val start = game.startTime.atZone(ZoneId.systemDefault())
                // Evening games in North America start the next day in Europe.
                if (start.toLocalDate() != gameDay) {
                    StatusText(start.format(DateTimeFormatter.ofPattern("EEE")))
                }
                StatusText(start.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)))
            }
            is GameStatus.Live -> {
                val color = MaterialTheme.colorScheme.error
                if (status.inIntermission) {
                    StatusText(stringResource(R.string.intermission, status.period), color, FontWeight.SemiBold)
                } else {
                    StatusText(status.period, color, FontWeight.SemiBold)
                    StatusText(status.clock, color)
                }
            }
            is GameStatus.Final -> StatusText(
                stringResource(
                    when (status.ending) {
                        Ending.Regulation -> R.string.final_score
                        Ending.Overtime -> R.string.final_ot
                        Ending.Shootout -> R.string.final_so
                    },
                ),
            )
            GameStatus.Postponed -> StatusText(stringResource(R.string.postponed))
        }
    }
}

@Composable
private fun StatusText(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    fontWeight: FontWeight? = null,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        fontWeight = fontWeight,
        textAlign = TextAlign.Center,
    )
}

/** Dims the losing side of a finished game. */
private fun Game.isTrailing(team: TeamLine): Boolean {
    if (status !is GameStatus.Final) return false
    val other = if (team == away) home else away
    return (team.score ?: 0) < (other.score ?: 0)
}

@Preview
@Composable
private fun GameCardPreview() {
    NhlScoresTheme {
        GameCard(
            game = Game(
                id = 1,
                startTime = Instant.parse("2026-10-11T21:00:00Z"),
                away = TeamLine(
                    "SEA", "Kraken", "", "", score = 5, record = null,
                    points = listOf(
                        PlayerPoints(8482665, "M. Beniers", goals = 2, assists = 1),
                        PlayerPoints(8481554, "K. Kakko", goals = 1, assists = 1),
                    ),
                ),
                home = TeamLine("WSH", "Capitals", "", "", score = 3, record = null, points = emptyList()),
                status = GameStatus.Final(Ending.Regulation),
            ),
            gameDay = LocalDate.parse("2026-10-11"),
            followed = FollowedPlayers(Country.Finland, setOf(8481554)),
            onClick = {},
        )
    }
}
