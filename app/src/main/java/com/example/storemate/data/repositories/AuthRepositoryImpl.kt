package com.example.storemate.data.repositories

import com.example.storemate.data.StoreMateDb
import com.example.storemate.data.local.SessionStore
import com.example.storemate.data.remote.NetworkConfig
import com.example.storemate.data.remote.ServerUrl
import com.example.storemate.data.remote.SessionExpiryNotifier
import com.example.storemate.data.remote.StoreMateApi
import com.example.storemate.data.remote.dto.AuthRequestDto
import com.example.storemate.data.remote.dto.AuthResponseDto
import com.example.storemate.data.remote.toUserFacingException
import com.example.storemate.domain.model.Session
import com.example.storemate.domain.repositories.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val api: StoreMateApi,
    private val sessionStore: SessionStore,
    private val networkConfig: NetworkConfig,
    private val db: StoreMateDb,
    private val sessionExpiryNotifier: SessionExpiryNotifier
) : AuthRepository {

    override val session: Flow<Session?> = sessionStore.sessionFlow

    override val lastServerUrl: Flow<String> = sessionStore.lastServerUrlFlow

    override val sessionExpired: Flow<Unit> = sessionExpiryNotifier.events

    override suspend fun restoreSession(): Session? {
        val stored = sessionStore.currentSession() ?: run {
            // Still prime the server address so a re-login hits the right box.
            sessionStore.currentServerUrlOrNull()?.let { applyServer(it) }
            return null
        }
        applyServer(stored.serverUrl)
        networkConfig.updateToken(stored.token)
        return stored
    }

    override suspend fun login(serverUrl: String, username: String, password: String) =
        authenticate(serverUrl) { api.login(AuthRequestDto(username.trim(), password)) }

    override suspend fun register(serverUrl: String, username: String, password: String) =
        authenticate(serverUrl) { api.register(AuthRequestDto(username.trim(), password)) }

    override suspend fun logout() {
        networkConfig.clear()
        sessionStore.clearCredentials()
        // The cache mirrors one store's data. Leaving it behind would show the
        // previous user's inventory to whoever signs in next.
        //
        // clearAllTables() blocks and refuses to run on the main thread, and both
        // callers (the Logout button and the session-expiry handler) are on it.
        withContext(Dispatchers.IO) { db.clearAllTables() }
    }

    private suspend fun authenticate(
        serverUrl: String,
        call: suspend () -> AuthResponseDto
    ): Result<Session> {
        val normalized = ServerUrl.normalize(serverUrl)
            ?: return Result.failure(
                IllegalArgumentException("That server address doesn't look right")
            )

        applyServer(normalized)
        sessionStore.rememberServerUrl(normalized)

        return try {
            val response = call()
            val session = Session(
                serverUrl = normalized,
                token = response.token,
                username = response.username,
                storeName = response.storeName
            )
            networkConfig.updateToken(session.token)
            sessionStore.save(session)
            Result.success(session)
        } catch (ex: Exception) {
            networkConfig.clear()
            Result.failure(ex.toUserFacingException())
        }
    }

    private fun applyServer(url: String) {
        networkConfig.updateServer(ServerUrl.parse(url))
    }
}
