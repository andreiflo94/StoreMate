package com.example.storemate.domain.repositories

import kotlinx.coroutines.flow.StateFlow

/** Where the local cache stands relative to the shop's server. */
sealed interface SyncState {
    data object Idle : SyncState
    data object Syncing : SyncState
    data class Synced(val atEpochMillis: Long) : SyncState
    /** The cache is still readable; it just may be out of date. */
    data class Failed(val message: String) : SyncState
}

/**
 * Pulls the shop's data from the server into the local cache.
 *
 * Reads in the app always come from the cache, so a sync failure degrades the
 * app to "showing possibly stale data" rather than breaking it.
 */
interface SyncRepository {

    val syncState: StateFlow<SyncState>

    /** Mirrors suppliers, products and transactions from the server. */
    suspend fun refreshAll(): Result<Unit>
}
