package com.lnevala.nhlscores.data

import com.lnevala.nhlscores.domain.Ending
import com.lnevala.nhlscores.domain.GameStatus
import com.lnevala.nhlscores.domain.PlayerPoints
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ScoreMapperTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun game(state: String, extra: String = "") = """
        {
          "id": 2026020084,
          "startTimeUTC": "2026-10-11T21:00:00Z",
          "gameState": "$state",
          "awayTeam": { "abbrev": "SEA", "name": { "default": "Kraken" }, "score": 5, "sog": 27,
                        "logo": "https://assets.nhle.com/logos/nhl/svg/SEA_light.svg" },
          "homeTeam": { "abbrev": "WSH", "name": { "default": "Capitals" }, "score": 3, "sog": 36,
                        "logo": "https://assets.nhle.com/logos/nhl/svg/WSH_secondary_light.svg" }
          $extra
        }
    """

    private fun map(gameJson: String) = json.decodeFromString<GameDto>(gameJson).toDomain()

    @Test
    fun `maps a day with its week`() {
        val day = json.decodeFromString<ScoreResponseDto>(
            """
            { "currentDate": "2026-10-11", "unknownField": true,
              "gameWeek": [ { "date": "2026-10-11", "dayAbbrev": "SUN", "numberOfGames": 3 } ],
              "games": [ ${game("OFF")} ] }
            """,
        ).toDomain()

        assertEquals(LocalDate.parse("2026-10-11"), day.date)
        assertEquals(3, day.week.single().gameCount)
        assertEquals("Kraken", day.games.single().away.name)
        assertEquals(5, day.games.single().away.score)
    }

    @Test
    fun `derives the dark logo`() {
        assertEquals("https://assets.nhle.com/logos/nhl/svg/SEA_dark.svg", map(game("OFF")).away.darkLogoUrl)
    }

    @Test
    fun `future games are scheduled`() {
        assertEquals(GameStatus.Scheduled, map(game("FUT")).status)
        assertEquals(GameStatus.Scheduled, map(game("PRE")).status)
    }

    @Test
    fun `postponed games are postponed whatever their state`() {
        assertEquals(GameStatus.Postponed, map(game("FUT", """, "gameScheduleState": "PPD" """)).status)
    }

    @Test
    fun `live games carry period and clock`() {
        val status = map(
            game(
                "LIVE",
                """, "clock": { "timeRemaining": "12:34", "inIntermission": false },
                     "periodDescriptor": { "number": 2, "periodType": "REG", "maxRegulationPeriods": 3 }""",
            ),
        ).status

        assertEquals(GameStatus.Live(period = "2nd", clock = "12:34", inIntermission = false), status)
    }

    @Test
    fun `finished games report how they ended`() {
        assertEquals(GameStatus.Final(Ending.Regulation), map(game("OFF")).status)
        assertEquals(
            GameStatus.Final(Ending.Overtime),
            map(game("FINAL", """, "gameOutcome": { "lastPeriodType": "OT" }""")).status,
        )
        assertEquals(
            GameStatus.Final(Ending.Shootout),
            map(game("OFF", """, "gameOutcome": { "lastPeriodType": "SO" }""")).status,
        )
    }

    @Test
    fun `adds up each team's points from the goals, ignoring shootout goals`() {
        fun goal(id: Long, name: String, team: String, period: String = "REG", vararg assists: Pair<Long, String>) = """
            { "playerId": $id, "name": { "default": "$name" }, "teamAbbrev": "$team",
              "periodDescriptor": { "number": 1, "periodType": "$period" },
              "assists": [ ${assists.joinToString { """{ "playerId": ${it.first}, "name": { "default": "${it.second}" } }""" }} ] }
        """
        val goals = listOf(
            goal(1, "K. Kakko", "SEA", "REG", 2L to "V. Dunn"),
            goal(2, "V. Dunn", "SEA", "REG", 1L to "K. Kakko", 3L to "A. Larsson"),
            goal(1, "K. Kakko", "SEA", "REG"),
            goal(9, "A. Ovechkin", "WSH", "REG"),
            goal(3, "A. Larsson", "SEA", "SO"),
        )

        val game = map(game("OFF", """, "goals": [ ${goals.joinToString()} ]"""))

        assertEquals(
            listOf(
                PlayerPoints(1, "K. Kakko", goals = 2, assists = 1),
                PlayerPoints(2, "V. Dunn", goals = 1, assists = 1),
                PlayerPoints(3, "A. Larsson", goals = 0, assists = 1),
            ),
            game.away.points,
        )
        assertEquals(listOf(PlayerPoints(9, "A. Ovechkin", goals = 1, assists = 0)), game.home.points)
    }

    @Test
    fun `keeps only last names, including multi-part ones`() {
        val goals = """
            { "playerId": 1, "name": { "default": "J. van Riemsdyk" }, "teamAbbrev": "SEA",
              "assists": [ { "playerId": 2, "name": { "default": "J.J. Moser" } },
                           { "playerId": 3, "name": { "default": "Kakko" } } ] }
        """

        val names = map(game("LIVE", """, "goals": [ $goals ]""")).away.points.map { it.lastName }

        assertEquals(listOf("van Riemsdyk", "Moser", "Kakko"), names)
    }

    @Test
    fun `games without goals have no points`() {
        assertEquals(emptyList<PlayerPoints>(), map(game("FUT")).away.points)
    }

    @Test
    fun `labels periods including playoff overtimes`() {
        assertEquals("1st", PeriodDescriptorDto(1, "REG").label())
        assertEquals("3rd", PeriodDescriptorDto(3, "REG").label())
        assertEquals("OT", PeriodDescriptorDto(4, "OT").label())
        assertEquals("3OT", PeriodDescriptorDto(6, "OT").label())
        assertEquals("SO", PeriodDescriptorDto(5, "SO").label())
    }
}
