package com.example.storemate.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storemate.domain.repositories.InventoryRepository
import com.example.storemate.presentation.UiState
import com.example.storemate.domain.model.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<DashboardData>>(UiState.Loading)
    val uiState: StateFlow<UiState<DashboardData>> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<QuickAccessType>()
    val effects = _effects

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val lowStock = repository.getLowStockProducts()
                val recentTransactionsWithProductName = ArrayList<TransactionWithProductName>()
                repository.getRecentTransactions(10).map { transaction ->
                    repository.getProductById(transaction.productId)?.name?.let{ transactionWithProductName ->
                        recentTransactionsWithProductName.add(TransactionWithProductName(
                            transaction = transaction,
                            productName = transactionWithProductName
                        ))
                    }
                }
                _uiState.value = UiState.Success(
                    DashboardData(
                        lowStockItems = lowStock,
                        recentTransactions = recentTransactionsWithProductName
                    )
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load dashboard: ${e.message}")
            }
        }
    }

    fun onQuickAccessClicked(type: QuickAccessType) {
        viewModelScope.launch {
            _effects.emit(type)
        }
    }
}