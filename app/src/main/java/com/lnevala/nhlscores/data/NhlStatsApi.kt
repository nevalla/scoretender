package com.lnevala.nhlscores.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** The public, undocumented stats API behind nhl.com/stats. */
interface NhlStatsApi {
    /** [position] is "skater" or "goalie"; [filter] is a Cayenne expression such as `nationalityCode="FIN"`. */
    @GET("stats/rest/en/{position}/bios")
    suspend fun bios(
        @Path("position") position: String,
        @Query("cayenneExp") filter: String,
        @Query("limit") limit: Int = -1,
    ): PlayerBiosDto

    companion object {
        const val BASE_URL = "https://api.nhle.com/"
    }
}
