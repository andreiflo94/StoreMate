package com.example.storemate.data.remote

import okhttp3.HttpUrl

/**
 * Mutable connection state shared by the OkHttp interceptors.
 *
 * Retrofit fixes its base URL at construction time, but an on-prem app only
 * learns the server address when the user types it on the login screen — and it
 * changes again if they point the app at a different shop. Holding it here lets
 * a single Retrofit instance follow whatever server is currently configured.
 */
class NetworkConfig {

    @Volatile
    var baseUrl: HttpUrl? = null
        private set

    @Volatile
    var token: String? = null
        private set

    fun updateServer(url: HttpUrl?) {
        baseUrl = url
    }

    fun updateToken(newToken: String?) {
        token = newToken
    }

    fun clear() {
        token = null
    }
}
