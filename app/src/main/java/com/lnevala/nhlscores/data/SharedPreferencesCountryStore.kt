package com.lnevala.nhlscores.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.lnevala.nhlscores.domain.Country
import com.lnevala.nhlscores.domain.CountryStore
import java.util.Locale

/**
 * Until the user picks one, follows the phone's own country if it has NHL players, otherwise Finland.
 * Following no country is saved as an empty code, so it isn't mistaken for not having picked yet.
 */
class SharedPreferencesCountryStore(private val preferences: SharedPreferences) : CountryStore {

    override fun load(): Country? =
        when (val code = preferences.getString(KEY, null)) {
            null -> phoneCountry() ?: Country.Finland
            NONE -> null
            else -> Country(code)
        }

    override fun save(country: Country?) = preferences.edit { putString(KEY, country?.code ?: NONE) }

    private fun phoneCountry(): Country? =
        runCatching { Country(Locale.getDefault().isO3Country) }.getOrNull()
            ?.takeIf { it in Country.withNhlPlayers }

    private companion object {
        const val KEY = "followed_country"
        const val NONE = ""
    }
}
