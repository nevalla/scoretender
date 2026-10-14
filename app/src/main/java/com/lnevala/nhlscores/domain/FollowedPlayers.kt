package com.lnevala.nhlscores.domain

import kotlinx.coroutines.flow.StateFlow

/** The players of the country the user follows, highlighted throughout the app; no country highlights no one. */
data class FollowedPlayers(val country: Country?, val playerIds: Set<Long> = emptySet()) {
    operator fun contains(playerId: Long) = playerId in playerIds
}

interface FollowedPlayersRepository {
    /** Starts with the chosen country; its players fill in once they've loaded. */
    val followed: StateFlow<FollowedPlayers>

    /** Null follows no country. */
    fun follow(country: Country?)

    /** Loads the players if they haven't loaded yet, e.g. after a failed attempt. Never throws. */
    suspend fun ensureLoaded()
}

/** Where the chosen country is kept between launches. */
interface CountryStore {
    fun load(): Country?
    fun save(country: Country?)
}
