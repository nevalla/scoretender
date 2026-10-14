package com.lnevala.nhlscores.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lnevala.nhlscores.domain.FollowedPlayers
import com.lnevala.nhlscores.domain.PlayerRef
import com.lnevala.nhlscores.ui.common.flag
import java.util.Locale

/** A titled card, the building block of the detail screen. */
@Composable
fun DetailSection(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

data class StatRow(val label: String, val values: List<String>)

/** A compact table: a label column followed by right-aligned number columns. */
@Composable
fun StatTable(columns: List<String>, rows: List<StatRow>, columnWidth: Dp = 52.dp) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row {
            Spacer(Modifier.weight(1f))
            columns.forEach {
                Text(
                    text = it,
                    modifier = Modifier.width(columnWidth),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )
            }
        }
        rows.forEach { row ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(row.label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                row.values.forEach {
                    Text(
                        text = it,
                        modifier = Modifier.width(columnWidth),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

/**
 * A player with their stats: the name gets the width so it fits on narrow phones, the details go
 * underneath, and the number that matters most stands out on the right.
 */
@Composable
fun PlayerStatRow(
    name: String,
    details: String,
    value: String,
    valueCaption: String? = null,
    emphasized: Boolean = false,
) {
    val weight = if (emphasized) FontWeight.SemiBold else null
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = weight)
            Text(details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = weight)
            valueCaption?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

fun playerName(player: PlayerRef, followed: FollowedPlayers) =
    if (player.id in followed) "${followed.country?.flag.orEmpty()} ${player.name}" else player.name

/** Hockey writes save percentage as ".917". */
fun formatSavePercentage(value: Double?): String =
    value?.let { String.format(Locale.ROOT, "%.3f", it).removePrefix("0") } ?: "–"

fun formatGoalsAgainstAverage(value: Double?): String =
    value?.let { String.format(Locale.ROOT, "%.2f", it) } ?: "–"

fun formatPlusMinus(value: Int): String = if (value > 0) "+$value" else value.toString()
