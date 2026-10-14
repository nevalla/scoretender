package com.lnevala.nhlscores.domain

/** [name] is the first initial and last name, e.g. "M. Tkachuk", as the NHL writes it. */
data class PlayerPoints(
    val playerId: Long,
    val name: String,
    val goals: Int,
    val assists: Int,
) {
    val points: Int get() = goals + assists

    /** Everything after the initial, so "J. van Riemsdyk" keeps "van Riemsdyk". */
    val lastName: String get() = name.substringAfter(". ")

    operator fun plus(other: PlayerPoints) = copy(goals = goals + other.goals, assists = assists + other.assists)
}
