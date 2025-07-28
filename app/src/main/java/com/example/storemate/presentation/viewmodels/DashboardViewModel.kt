package com.example.storemate.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storemate.domain.model.DashboardEffect
import com.example.storemate.domain.model.DashboardIntent
import com.example.storemate.domain.model.DashboardScreenState
import com.example.storemate.domain.repositories.InventoryRepository
import com.example.storemate.presentation.UiState
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
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<DashboardScreenState>>(UiState.Loading)
    val uiState: StateFlow<UiState<DashboardScreenState>> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<DashboardEffect>()
    val effects: SharedFlow<DashboardEffect> = _effects.asSharedFlow()

    init {
        observeData()
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
            }
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.getLowStockProductsFlow(),
                repository.getRecentTransactionsWithProductNameFlow(limit = 10)
            ) { lowStock, transactions ->
                DashboardScreenState(
                    lowStockItems = lowStock,
                    recentTransactions = transactions
                )
            }.catch { e ->
                _uiState.value = UiState.Error("Failed to load dashboard: ${e.message}")
            }.collect { state ->
                updateState { state }
            }
        }
    }

    private fun updateState(reducer: () -> DashboardScreenState) {
        _uiState.value = UiState.Success(reducer())
    }

    private suspend fun emitEffect(effect: DashboardEffect) {
        _effects.emit(effect)
    }
}
