package com.example.storemate.data.remote

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Fired by [AuthInterceptor] when an authenticated request comes back 401.
 *
 * Any screen can trigger this, not just the one that happened to make the
 * failing call, so it's a standalone signal rather than something each
 * ViewModel maps its own errors into.
 */
class SessionExpiryNotifier {

    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    fun signal() {
        _events.tryEmit(Unit)
    }
}
