package com.lnevala.nhlscores.ui.scores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.lnevala.nhlscores.R
import com.lnevala.nhlscores.domain.Country
import com.lnevala.nhlscores.ui.common.displayName
import com.lnevala.nhlscores.ui.common.flag

/** Picks whose players get highlighted, or no one's; picking closes the dialog straight away. */
@Composable
fun CountryPickerDialog(
    selected: Country?,
    onDismiss: () -> Unit,
    onCountryPicked: (Country?) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.highlight_players_from)) },
        text = {
            LazyColumn {
                items(listOf(null) + Country.withNhlPlayers, key = { it?.code.orEmpty() }) { country ->
                    CountryRow(
                        country = country,
                        selected = country == selected,
                        onClick = {
                            onCountryPicked(country)
                            onDismiss()
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun CountryRow(country: Country?, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        CountryMark(country)
        Text(
            text = country?.displayName ?: stringResource(R.string.no_country),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

/** The country's flag, or a crossed-out globe when no country is followed. Purely decorative. */
@Composable
fun CountryMark(country: Country?) {
    Box(Modifier.size(28.dp).clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
        if (country == null) {
            Icon(painterResource(R.drawable.ic_no_country), contentDescription = null)
        } else {
            Text(country.flag, style = MaterialTheme.typography.titleLarge)
        }
    }
}
