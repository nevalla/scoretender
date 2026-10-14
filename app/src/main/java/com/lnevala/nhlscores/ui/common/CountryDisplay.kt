package com.lnevala.nhlscores.ui.common

import com.lnevala.nhlscores.domain.Country
import java.util.Locale

/** Two-letter codes by three-letter code, since flags and Android's country names use the former. */
private val alpha2ByAlpha3: Map<String, String> by lazy {
    Locale.getISOCountries().associateBy { region(it).isO3Country }
}

private val Country.alpha2: String? get() = alpha2ByAlpha3[code]

/** The flag emoji, built from the two-letter code's regional indicator letters, e.g. 🇫🇮. */
val Country.flag: String
    get() = alpha2?.map { Character.toChars(REGIONAL_INDICATOR_A + (it - 'A')).concatToString() }?.joinToString("") ?: ""

/** The country's name in the phone's language, e.g. "Finland" or "Suomi". */
val Country.displayName: String
    get() = alpha2?.let { region(it).displayCountry } ?: code

private const val REGIONAL_INDICATOR_A = 0x1F1E6

private fun region(alpha2: String): Locale = Locale.Builder().setRegion(alpha2).build()
