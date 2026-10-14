package com.lnevala.nhlscores.data

import com.lnevala.nhlscores.domain.Ending
import com.lnevala.nhlscores.domain.GameStatus
import com.lnevala.nhlscores.domain.LeaderCategory
import com.lnevala.nhlscores.domain.PlayerRef
import com.lnevala.nhlscores.domain.SeasonSeries
import com.lnevala.nhlscores.domain.TeamStatKind
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Runs against real responses: SEA 5–3 WSH on 2026-10-11, and MTL at WSH before puck drop. */
class GameDetailMapperTest {

    private val json = Json { ignoreUnknownKeys = true }

    private inline fun <reified T> fixture(name: String): T =
        json.decodeFromString(javaClass.getResource("/gamecenter/$name.json")!!.readText())

    private fun detail(game: String) = gameDetail(
        fixture("$game-landing"),
        fixture("$game-right-rail"),
        fixture("$game-boxscore"),
    )

    private val final = detail("final")
    private val future = detail("future")

    @Test
    fun `header has teams, score and status`() {
        assertEquals("Kraken", final.game.away.name)
        assertEquals("https://assets.nhle.com/logos/nhl/svg/SEA_dark.svg", final.game.away.darkLogoUrl)
        assertEquals(5, final.game.away.score)
        assertEquals(3, final.game.home.score)
        assertEquals(GameStatus.Final(Ending.Regulation), final.game.status)
        assertEquals("Capital One Arena", final.venue)
    }

    @Test
    fun `line score per period`() {
        assertEquals(listOf("1st", "2nd", "3rd"), final.periods.map { it.period })
        assertEquals(listOf(2, 1, 2), final.periods.map { it.away })
        assertEquals(listOf(2, 0, 1), final.periods.map { it.home })
    }

    @Test
    fun `scoring summary`() {
        assertEquals(8, final.goals.size)
        val first = final.goals.first()
        assertEquals("1st", first.period)
        assertEquals("06:06", first.time)
        assertEquals("SEA", first.teamAbbrev)
        assertEquals(PlayerRef(8481554, "K. Kakko"), first.scorer)
        assertEquals(listOf("V. Dunn", "A. Larsson"), first.assists.map { it.name })
        assertNull(first.situation)
        assertEquals(1 to 0, first.awayScore to first.homeScore)
        assertNotNull(first.videoUrl)
        assertEquals(5 to 3, final.goals.last().let { it.awayScore to it.homeScore })
    }

    @Test
    fun `team stats`() {
        val stats = final.teamStats.associateBy { it.kind }
        assertEquals(TeamStatKind.entries.toSet(), stats.keys)
        assertEquals("27" to "36", stats.getValue(TeamStatKind.Shots).let { it.awayLabel to it.homeLabel })
        assertEquals("48%" to "52%", stats.getValue(TeamStatKind.FaceoffPercentage).let { it.awayLabel to it.homeLabel })
        assertEquals("0/2", stats.getValue(TeamStatKind.PowerPlay).awayLabel)
        assertEquals(0.0, stats.getValue(TeamStatKind.PowerPlay).awayValue, 0.0)
    }

    @Test
    fun `goalies who played`() {
        val daccord = final.goalies.single { it.teamAbbrev == "SEA" }
        assertEquals("J. Daccord", daccord.player.name)
        assertEquals(33 to 36, daccord.saves to daccord.shotsAgainst)
        assertEquals("W", daccord.decision)
        assertTrue("backup who didn't play is left out", final.goalies.none { it.player.name == "P. Grubauer" })
    }

    @Test
    fun `skaters of both teams`() {
        val kakko = final.skaters.single { it.player.id == 8481554L }
        assertEquals("SEA", kakko.teamAbbrev)
        assertEquals(1, kakko.goals)
        assertTrue(final.skaters.any { it.teamAbbrev == "WSH" })
    }

    @Test
    fun `three stars in order`() {
        assertEquals(listOf(1, 2, 3), final.threeStars.map { it.rank })
        assertEquals("J. McCann", final.threeStars.first().player.name)
    }

    @Test
    fun `played games have no preview`() {
        assertNull(final.preview)
    }

    @Test
    fun `upcoming games have a preview instead of game events`() {
        assertEquals(GameStatus.Scheduled, future.game.status)
        assertTrue(future.periods.isEmpty())
        assertTrue(future.goals.isEmpty())
        assertTrue(future.goalies.isEmpty())

        val preview = future.preview!!
        assertEquals(listOf(LeaderCategory.Points, LeaderCategory.Goals, LeaderCategory.Assists), preview.leaders.map { it.category })
        assertEquals("N. Suzuki", preview.leaders.first().away?.player?.name)
        assertEquals(listOf("J. Fowler", "J. Dobes", "L. Thompson", "C. Lindgren"), preview.goalies.map { it.player.name })
        assertEquals(listOf("MTL", "MTL", "WSH", "WSH"), preview.goalies.map { it.teamAbbrev })
        assertEquals(SeasonSeries(0, 0), preview.seasonSeries)
    }

    @Test
    fun `final status falls back to the last period when the outcome is missing`() {
        val status = gameStatus("OFF", "OK", null, PeriodDescriptorDto(4, "OT"), gameOutcome = null)
        assertEquals(GameStatus.Final(Ending.Overtime), status)
    }
}
