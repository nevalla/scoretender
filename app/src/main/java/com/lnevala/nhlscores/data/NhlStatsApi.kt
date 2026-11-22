package com.lnevala.nhlscores.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** The public, undocumented stats API behind nhl.com/stats. */
interface NhlStatsApi {
    /**
     * [position] is "skater" or "goalie"; [filter] is a Cayenne expression such as `nationalityCode="FIN"`.
     * Returns one page of players from [start] on, and how many match in all.
     */
    @GET("stats/rest/en/{position}/bios")
    suspend fun bios(
        @Path("position") position: String,
        @Query("cayenneExp") filter: String,
        @Query("start") start: Int = 0,
        @Query("limit") limit: Int = PAGE_SIZE,
        @Query("sort") sort: String = BY_PLAYER_ID,
    ): PlayerBiosDto

    companion object {
        const val BASE_URL = "https://api.nhle.com/"

        /** The most players the API returns at once; it caps larger limits, -1 included, to this for now. */
        const val PAGE_SIZE = 100

        /** A fixed order, so that pages don't overlap or skip players. */
        const val BY_PLAYER_ID = """[{"property":"playerId","direction":"ASC"}]"""
    }
}
