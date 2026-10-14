package com.lnevala.nhlscores.ui.scores

import com.lnevala.nhlscores.domain.FollowedPlayers
import com.lnevala.nhlscores.domain.PlayerPoints
import com.lnevala.nhlscores.domain.TeamLine
import com.lnevala.nhlscores.ui.common.flag

/** A big game is more than two points. */
private const val BIG_GAME_POINTS = 3

/** Followed players with any points, then anyone else having a big game; best first within each. */
fun TeamLine.highlightedPlayers(followed: FollowedPlayers): List<PlayerPoints> =
    points
        .filter { it.playerId in followed || it.points >= BIG_GAME_POINTS }
        .sortedByDescending { it.playerId in followed }

/**
 * Goals+assists, with the followed players flagged: "🇫🇮 Aho 1+1, McDavid 1+3". Last names save space,
 * except when two of the team's scorers share one: "M. Tkachuk 0+5, B. Tkachuk 3+1".
 */
fun TeamLine.highlightsText(players: List<PlayerPoints>, followed: FollowedPlayers): String {
    val sharedLastNames = points.groupingBy { it.lastName }.eachCount().filterValues { it > 1 }.keys
    return players.joinToString { player ->
        val flag = if (player.playerId in followed) "${followed.country?.flag.orEmpty()} " else ""
        val name = if (player.lastName in sharedLastNames) player.name else player.lastName
        "$flag$name ${player.goals}+${player.assists}"
    }
}
