package com.lnevala.nhlscores.domain

/** A country by its ISO 3166 alpha-3 code, the form the NHL uses, e.g. "FIN". */
@JvmInline
value class Country(val code: String) {
    companion object {
        val Finland = Country("FIN")

        /** Countries with NHL players, most players first. */
        val withNhlPlayers = listOf(
            "CAN", "USA", "SWE", "RUS", "FIN", "CZE", "CHE", "SVK", "BLR",
            "DEU", "LVA", "DNK", "AUT", "NOR", "SVN", "AUS", "FRA",
        ).map(::Country)
    }
}
