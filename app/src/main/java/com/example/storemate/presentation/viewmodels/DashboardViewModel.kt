package com.example.storemate.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storemate.domain.repositories.AuthRepository
import com.example.storemate.domain.repositories.InventoryRepository
import com.example.storemate.domain.repositories.SyncRepository
import com.example.storemate.domain.repositories.SyncState
import com.example.storemate.presentation.UiState
import com.example.storemate.presentation.common.DashboardEffect
import com.example.storemate.presentation.common.DashboardIntent
import com.example.storemate.presentation.common.DashboardScreenState
import com.example.storemate.presentation.common.SyncStatusUi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: InventoryRepository,
    private val syncRepository: SyncRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<DashboardScreenState>>(UiState.Loading)
    val uiState: StateFlow<UiState<DashboardScreenState>> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<DashboardEffect>()
    val effects: SharedFlow<DashboardEffect> = _effects.asSharedFlow()

    init {
        observeData()
        // Opening the dashboard is the natural moment to pick up changes made
        // on the shop's other devices.
        refresh(announce = false)
    }

    fun onIntent(intent: DashboardIntent) {
        viewModelScope.launch {
            when (intent) {
                DashboardIntent.NavigateToProducts ->
                    emitEffect(DashboardEffect.NavigateToProductsEffect)

                DashboardIntent.NavigateToSuppliers ->
                    emitEffect(DashboardEffect.NavigateToSuppliersEffect)

                DashboardIntent.NavigateToStockManagement ->
                    emitEffect(DashboardEffect.NavigateToStockManagementEffect)

                DashboardIntent.NavigateToTransactions ->
                    emitEffect(DashboardEffect.NavigateToTransactionsEffect)

                DashboardIntent.NavigateToImport ->
                    emitEffect(DashboardEffect.NavigateToImportEffect)

                DashboardIntent.Refresh -> refresh(announce = true)

                DashboardIntent.Logout -> {
                    authRepository.logout()
                    emitEffect(DashboardEffect.LoggedOut)
                }
            }
        }
    }

    /**
     * A failed sync is not an error state: the cache still renders. It only
     * gets announced when the user explicitly asked to refresh.
     */
    private fun refresh(announce: Boolean) {
        viewModelScope.launch {
            syncRepository.refreshAll()
                .onSuccess {
                    if (announce) emitEffect(DashboardEffect.ShowMessageToUi("Up to date"))
                }
                .onFailure { error ->
                    if (announce) {
                        emitEffect(
                            DashboardEffect.ShowErrorToUi(
                                error.message ?: "Could not reach the server"
                            )
                        )
                    }
                }
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.getLowStockProductsFlow(),
                repository.getRecentTransactionsWithProductNameFlow(limit = 10),
                syncRepository.syncState,
                authRepository.session
            ) { lowStock, transactions, syncState, session ->
                DashboardScreenState(
                    lowStockItems = lowStock,
                    recentTransactions = transactions,
                    storeName = session?.storeName.orEmpty(),
                    syncStatus = syncState.toUi()
                )
            }.catch { e ->
                _uiState.value = UiState.Error("Failed to load dashboard: ${e.message}")
            }.collect { state ->
                updateState { state }
            }
        }
    }

    private fun SyncState.toUi(): SyncStatusUi = when (this) {
        SyncState.Idle -> SyncStatusUi.Idle
        SyncState.Syncing -> SyncStatusUi.Syncing
        is SyncState.Synced -> SyncStatusUi.Synced(atEpochMillis)
        is SyncState.Failed -> SyncStatusUi.Offline(message)
    }

    private fun updateState(reducer: () -> DashboardScreenState) {
        _uiState.value = UiState.Success(reducer())
    }

    private suspend fun emitEffect(effect: DashboardEffect) {
        _effects.emit(effect)
    }
}
