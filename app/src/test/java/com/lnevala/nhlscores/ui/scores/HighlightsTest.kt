package com.lnevala.nhlscores.ui.scores

import com.lnevala.nhlscores.domain.Country
import com.lnevala.nhlscores.domain.FollowedPlayers
import com.lnevala.nhlscores.domain.PlayerPoints
import com.lnevala.nhlscores.domain.TeamLine
import org.junit.Assert.assertEquals
import org.junit.Test

class HighlightsTest {

    private val mcDavid = PlayerPoints(1, "C. McDavid", goals = 1, assists = 3)
    private val draisaitl = PlayerPoints(2, "L. Draisaitl", goals = 1, assists = 1)
    private val aho = PlayerPoints(3, "S. Aho", goals = 0, assists = 1)
    private val barkov = PlayerPoints(4, "A. Barkov", goals = 2, assists = 2)
    private val finnish = FollowedPlayers(Country.Finland, setOf(aho.playerId, barkov.playerId))

    private fun team(vararg points: PlayerPoints) =
        TeamLine("EDM", "Oilers", "", "", score = 4, record = null, points = points.toList())

    @Test
    fun `highlights Finnish scorers first, then anyone with more than two points`() {
        assertEquals(
            listOf(barkov, aho, mcDavid),
            team(barkov, mcDavid, draisaitl, aho).highlightedPlayers(finnish),
        )
    }

    @Test
    fun `two points is not enough for a non-Finn`() {
        assertEquals(emptyList<PlayerPoints>(), team(draisaitl).highlightedPlayers(finnish))
    }

    @Test
    fun `flags only the Finns`() {
        assertEquals("🇫🇮 Barkov 2+2, McDavid 1+3", team(barkov, mcDavid).highlightsText(listOf(barkov, mcDavid), finnish))
    }

    @Test
    fun `uses the flag of whichever country is followed`() {
        val swedish = FollowedPlayers(Country("SWE"), setOf(draisaitl.playerId))
        assertEquals("🇸🇪 Draisaitl 1+1", team(draisaitl).highlightsText(listOf(draisaitl), swedish))
    }

    @Test
    fun `with no country followed only big games stand out, unflagged`() {
        val nobody = FollowedPlayers(null)
        val highlighted = team(barkov, mcDavid, aho).highlightedPlayers(nobody)
        assertEquals(listOf(barkov, mcDavid), highlighted)
        assertEquals("Barkov 2+2, McDavid 1+3", team(barkov, mcDavid, aho).highlightsText(highlighted, nobody))
    }

    @Test
    fun `adds the initial when two scorers on the team share a last name`() {
        val matthew = PlayerPoints(5, "M. Tkachuk", goals = 0, assists = 5)
        val brady = PlayerPoints(6, "B. Tkachuk", goals = 3, assists = 1)
        val team = team(matthew, brady, barkov)
        assertEquals(
            "M. Tkachuk 0+5, B. Tkachuk 3+1, Barkov 2+2",
            team.highlightsText(listOf(matthew, brady, barkov), FollowedPlayers(null)),
        )
    }
}
