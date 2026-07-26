package com.example.storemate.domain.model

/**
 * A logged-in shop user, bound to the on-prem server they authenticated against.
 *
 * The server address is part of the session because an on-prem install has no
 * fixed URL — each shop points the app at its own machine.
 */
data class Session(
    val serverUrl: String,
    val token: String,
    val username: String,
    val storeName: String
)
