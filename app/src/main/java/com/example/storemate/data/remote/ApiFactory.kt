package com.example.storemate.data.remote

import com.example.storemate.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds the single Retrofit client. Its base URL is a placeholder that
 * [ServerUrlInterceptor] rewrites per request — see [NetworkConfig].
 */
object ApiFactory {

    /** Never actually called; only needs to be a syntactically valid base URL. */
    private const val PLACEHOLDER_BASE_URL = "http://storemate.invalid/"

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    fun createOkHttpClient(config: NetworkConfig, sessionExpiryNotifier: SessionExpiryNotifier): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(ServerUrlInterceptor(config))
            .addInterceptor(AuthInterceptor(config, sessionExpiryNotifier))
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BASIC
                        }
                    )
                }
            }
            // A LAN server should answer fast; failing quickly keeps the UI
            // responsive when the shop PC is off.
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

    fun createApi(client: OkHttpClient): StoreMateApi =
        Retrofit.Builder()
            .baseUrl(PLACEHOLDER_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(StoreMateApi::class.java)
}
