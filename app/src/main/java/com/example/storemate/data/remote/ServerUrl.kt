package com.example.storemate.data.remote

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Normalises whatever a shop owner types into the server-address field.
 *
 * They will type `192.168.1.10:8080`, `http://storemate.local`, or paste a URL
 * with a trailing slash — all of which should work.
 */
object ServerUrl {

    private const val DEFAULT_PORT = 8080

    /** Returns a normalised absolute URL, or null if it cannot be parsed. */
    fun normalize(raw: String): String? = parse(raw)?.toString()

    fun parse(raw: String): HttpUrl? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null

        // Trailing slashes are left for OkHttp to fold into the path — trimming
        // them ourselves can eat the "://" separator too (e.g. "http://" would
        // shrink to "http:", which then looks like a bare host named "http").
        val hasScheme = trimmed.contains("://")
        val withScheme = if (hasScheme) trimmed else "http://$trimmed"

        val url = withScheme.toHttpUrlOrNull() ?: return null
        if (url.host.isBlank()) return null

        // Only add the default port when the user gave neither a port nor an
        // explicit scheme, so `https://shop.example.com` stays on 443.
        val needsDefaultPort = !hasScheme && !hasExplicitPort(trimmed)
        return if (needsDefaultPort) url.newBuilder().port(DEFAULT_PORT).build() else url
    }

    private fun hasExplicitPort(hostAndMaybePort: String): Boolean {
        val hostPart = hostAndMaybePort.substringBefore('/')
        val colon = hostPart.lastIndexOf(':')
        return colon != -1 && hostPart.drop(colon + 1).toIntOrNull() != null
    }
}
