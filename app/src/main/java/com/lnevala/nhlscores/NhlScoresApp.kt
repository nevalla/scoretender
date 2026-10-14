package com.lnevala.nhlscores

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.lnevala.nhlscores.data.DefaultFollowedPlayersRepository
import com.lnevala.nhlscores.data.NhlApi
import com.lnevala.nhlscores.data.NhlGameDetailRepository
import com.lnevala.nhlscores.data.NhlPlayerNationalityRepository
import com.lnevala.nhlscores.data.NhlScoresRepository
import com.lnevala.nhlscores.data.NhlStatsApi
import com.lnevala.nhlscores.data.SharedPreferencesCountryStore
import com.lnevala.nhlscores.domain.FollowedPlayersRepository
import com.lnevala.nhlscores.domain.GameDetailRepository
import com.lnevala.nhlscores.domain.PlayerNationalityRepository
import com.lnevala.nhlscores.domain.ScoresRepository
import kotlinx.coroutines.MainScope
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class NhlScoresApp : Application(), SingletonImageLoader.Factory {

    private val httpClient by lazy { OkHttpClient() }
    private val json = Json { ignoreUnknownKeys = true }
    private val nhlApi by lazy { api<NhlApi>(NhlApi.BASE_URL) }

    val scoresRepository: ScoresRepository by lazy {
        NhlScoresRepository(nhlApi)
    }

    val gameDetailRepository: GameDetailRepository by lazy {
        NhlGameDetailRepository(nhlApi)
    }

    private val playerNationalityRepository: PlayerNationalityRepository by lazy {
        NhlPlayerNationalityRepository(api(NhlStatsApi.BASE_URL))
    }

    /** App-wide, so both screens see a newly picked country at once. */
    val followedPlayersRepository: FollowedPlayersRepository by lazy {
        DefaultFollowedPlayersRepository(
            SharedPreferencesCountryStore(getSharedPreferences("settings", MODE_PRIVATE)),
            playerNationalityRepository,
            MainScope(),
        )
    }

    private inline fun <reified T> api(baseUrl: String): T =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(T::class.java)

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .crossfade(true)
            .build()
}
