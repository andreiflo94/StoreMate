package com.example.storemate.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storemate.domain.repositories.AuthRepository
import com.example.storemate.domain.repositories.SyncRepository
import com.example.storemate.presentation.common.LoginEffect
import com.example.storemate.presentation.common.LoginIntent
import com.example.storemate.presentation.common.LoginScreenState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginScreenState())
    val uiState: StateFlow<LoginScreenState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<LoginEffect>()
    val effects: SharedFlow<LoginEffect> = _effects.asSharedFlow()

    init {
        // Prefill the shop's server address so staff only type credentials.
        viewModelScope.launch {
            val remembered = authRepository.lastServerUrl.first()
            if (remembered.isNotBlank()) {
                _uiState.value = _uiState.value.copy(serverUrl = remembered)
            }
        }
    }

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.ServerUrlChanged ->
                update { it.copy(serverUrl = intent.serverUrl, errorMessage = null) }

            is LoginIntent.UsernameChanged ->
                update { it.copy(username = intent.username, errorMessage = null) }

            is LoginIntent.PasswordChanged ->
                update { it.copy(password = intent.password, errorMessage = null) }

            LoginIntent.ToggleRegisterMode ->
                update { it.copy(isRegisterMode = !it.isRegisterMode, errorMessage = null) }

            LoginIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.isSubmitting || !state.isValid()) return

        viewModelScope.launch {
            update { it.copy(isSubmitting = true, errorMessage = null) }

            val result = if (state.isRegisterMode) {
                authRepository.register(state.serverUrl, state.username, state.password)
            } else {
                authRepository.login(state.serverUrl, state.username, state.password)
            }

            result
                .onSuccess {
                    // Pull the store down before showing the dashboard, so it
                    // never opens on an empty or previous user's cache.
                    syncRepository.refreshAll()
                    update { it.copy(isSubmitting = false, password = "") }
                    _effects.emit(LoginEffect.LoggedIn)
                }
                .onFailure { error ->
                    val message = error.message ?: "Could not sign in"
                    update { it.copy(isSubmitting = false, errorMessage = message) }
                    _effects.emit(LoginEffect.ShowError(message))
                }
        }
    }

    private fun update(transform: (LoginScreenState) -> LoginScreenState) {
        _uiState.value = transform(_uiState.value)
    }
}
