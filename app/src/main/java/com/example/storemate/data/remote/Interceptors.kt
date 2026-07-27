package com.example.storemate.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Redirects every request from Retrofit's placeholder base URL to the server
 * the shop actually configured.
 */
class ServerUrlInterceptor(private val config: NetworkConfig) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val server = config.baseUrl
            ?: throw IOException("No StoreMate server configured — sign in first")

        val request = chain.request()
        val rewritten = request.url.newBuilder()
            .scheme(server.scheme)
            .host(server.host)
            .port(server.port)
            // Keep any path the shop's address carries (a reverse-proxied install
            // like http://shop.local/storemate). Dropping it silently sent every
            // call to /api/... on the proxy root, which just 404s.
            .encodedPath(server.encodedPath.trimEnd('/') + request.url.encodedPath)
            .build()

        return chain.proceed(request.newBuilder().url(rewritten).build())
    }
}

/**
 * Attaches the bearer token to every request except the public auth endpoints,
 * and reports a rejected token so the app can react from wherever it is.
 */
class AuthInterceptor(
    private val config: NetworkConfig,
    private val sessionExpiryNotifier: SessionExpiryNotifier
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = config.token
        // Suffix match, not equality: [ServerUrlInterceptor] may have prefixed the
        // path with the server's own base path before this interceptor runs.
        val isPublic = PUBLIC_PATHS.any { request.url.encodedPath.endsWith(it) }

        val authorized = if (token.isNullOrBlank() || isPublic) {
            request
        } else {
            request.newBuilder().header("Authorization", "Bearer $token").build()
        }

        val response = chain.proceed(authorized)

        // A 401 on the login/register endpoints means bad credentials, not an
        // expired session — only flag it for requests that actually carried a token.
        if (!isPublic && response.code == 401) {
            sessionExpiryNotifier.signal()
        }

        return response
    }

    private companion object {
        val PUBLIC_PATHS = setOf("/api/auth/login", "/api/auth/register")
    }
}
