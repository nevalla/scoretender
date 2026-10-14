package com.lnevala.nhlscores.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lnevala.nhlscores.R
import com.lnevala.nhlscores.domain.GameDetail
import com.lnevala.nhlscores.domain.GameStatus
import com.lnevala.nhlscores.domain.TeamLine
import com.lnevala.nhlscores.ui.common.logoForTheme
import com.lnevala.nhlscores.ui.scores.StatusLabel

@Composable
fun GameHeader(detail: GameDetail) {
    val game = detail.game
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TeamColumn(game.away, Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (game.status !is GameStatus.Scheduled && game.status !is GameStatus.Postponed) {
                        Text(
                            text = "${game.away.score ?: 0} – ${game.home.score ?: 0}",
                            style = MaterialTheme.typography.displaySmall,
                        )
                    }
                    StatusLabel(game, detail.gameDay)
                }
                TeamColumn(game.home, Modifier.weight(1f))
            }
            if (detail.periods.isNotEmpty()) {
                HorizontalDivider()
                StatTable(
                    columns = detail.periods.map { it.period } + stringResource(R.string.column_total),
                    rows = listOf(
                        StatRow(game.away.abbrev, values = detail.periods.map { it.away.toString() } + "${game.away.score ?: 0}"),
                        StatRow(game.home.abbrev, values = detail.periods.map { it.home.toString() } + "${game.home.score ?: 0}"),
                    ),
                    columnWidth = 40.dp,
                )
            }
        }
    }
}

@Composable
private fun TeamColumn(team: TeamLine, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = team.logoForTheme(),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
        )
        Text(team.name, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}
