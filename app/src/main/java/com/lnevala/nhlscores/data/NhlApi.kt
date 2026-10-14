package com.lnevala.nhlscores.data

import retrofit2.http.GET
import retrofit2.http.Path

/** The public, undocumented API behind nhl.com. */
interface NhlApi {
    @GET("v1/score/now")
    suspend fun currentScore(): ScoreResponseDto

    @GET("v1/score/{date}")
    suspend fun score(@Path("date") date: String): ScoreResponseDto

    @GET("v1/gamecenter/{id}/landing")
    suspend fun landing(@Path("id") gameId: Long): LandingDto

    @GET("v1/gamecenter/{id}/right-rail")
    suspend fun rightRail(@Path("id") gameId: Long): RightRailDto

    @GET("v1/gamecenter/{id}/boxscore")
    suspend fun boxscore(@Path("id") gameId: Long): BoxscoreDto

    companion object {
        const val BASE_URL = "https://api-web.nhle.com/"
    }
}
