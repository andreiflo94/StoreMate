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
import kotlinx.coroutines.flow.StateFlow
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
    val effects = _effects

    init {
        observeDashboardData()
    }

    private fun observeDashboardData() {
        viewModelScope.launch {
            combine(
                repository.getLowStockProductsFlow(),
                repository.getRecentTransactionsWithProductNameFlow(limit = 10)
            ) { lowStockProducts, recentTransactions ->
                DashboardScreenState(
                    lowStockItems = lowStockProducts,
                    recentTransactions = recentTransactions
                )
            }.catch { e ->
                _uiState.value = UiState.Error("Failed to load dashboard: ${e.message}")
            }.collect { dashboardState ->
                _uiState.value = UiState.Success(dashboardState)
            }
        }
    }

    fun onIntent(dashboardIntent: DashboardIntent) {
        viewModelScope.launch {
            when (dashboardIntent) {
                DashboardIntent.NavigateToProducts -> _effects.emit(DashboardEffect.NavigateToProductsEffect)
                DashboardIntent.NavigateToStockManagement -> _effects.emit(DashboardEffect.NavigateToStockManagementEffect)
                DashboardIntent.NavigateToSuppliers -> _effects.emit(DashboardEffect.NavigateToSuppliersEffect)
                DashboardIntent.NavigateToTransactions -> _effects.emit(DashboardEffect.NavigateToTransactionsEffect)
            }
        }
    }
}
