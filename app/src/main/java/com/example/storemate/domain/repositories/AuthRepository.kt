package com.example.storemate.domain.repositories

import com.example.storemate.domain.model.Session
import kotlinx.coroutines.flow.Flow

/**
 * Owns the connection to a shop's on-prem server: which server, and who is
 * signed in to it.
 */
interface AuthRepository {

    /** Emits the active session, or null when signed out. */
    val session: Flow<Session?>

    /** The server address to prefill on the login screen. */
    val lastServerUrl: Flow<String>

    /** Emits whenever the server rejects the stored token as expired/invalid. */
    val sessionExpired: Flow<Unit>

    /** Restores a persisted session into the network layer on app start. */
    suspend fun restoreSession(): Session?

    suspend fun login(serverUrl: String, username: String, password: String): Result<Session>

    suspend fun register(serverUrl: String, username: String, password: String): Result<Session>

    /** Signs out and drops the locally cached store data. */
    suspend fun logout()
}
