package com.example.storemate.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.storemate.domain.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "storemate_session")

/**
 * Persists the logged-in session across app restarts.
 *
 * Also remembers the last server address even after logout, so staff do not
 * have to retype their shop's server URL every shift.
 */
class SessionStore(private val context: Context) {

    val sessionFlow: Flow<Session?> = context.sessionDataStore.data.map { prefs ->
        val serverUrl = prefs[KEY_SERVER_URL]
        val token = prefs[KEY_TOKEN]
        if (serverUrl.isNullOrBlank() || token.isNullOrBlank()) {
            null
        } else {
            Session(
                serverUrl = serverUrl,
                token = token,
                username = prefs[KEY_USERNAME].orEmpty(),
                storeName = prefs[KEY_STORE_NAME].orEmpty()
            )
        }
    }

    /** The last server address the user typed, whether or not they are logged in. */
    val lastServerUrlFlow: Flow<String> =
        context.sessionDataStore.data.map { it[KEY_SERVER_URL].orEmpty() }

    suspend fun currentSession(): Session? = sessionFlow.first()

    suspend fun currentServerUrlOrNull(): String? = lastServerUrlFlow.first().takeIf { it.isNotBlank() }

    suspend fun save(session: Session) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_SERVER_URL] = session.serverUrl
            prefs[KEY_TOKEN] = session.token
            prefs[KEY_USERNAME] = session.username
            prefs[KEY_STORE_NAME] = session.storeName
        }
    }

    suspend fun rememberServerUrl(serverUrl: String) {
        context.sessionDataStore.edit { it[KEY_SERVER_URL] = serverUrl }
    }

    /** Clears credentials but keeps the server address for the next login. */
    suspend fun clearCredentials() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_USERNAME)
            prefs.remove(KEY_STORE_NAME)
        }
    }

    private companion object {
        val KEY_SERVER_URL = stringPreferencesKey("server_url")
        val KEY_TOKEN = stringPreferencesKey("token")
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_STORE_NAME = stringPreferencesKey("store_name")
    }
}
