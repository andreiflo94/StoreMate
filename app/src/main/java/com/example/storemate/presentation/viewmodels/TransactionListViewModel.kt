package com.example.storemate.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storemate.domain.model.TransactionListEffect
import com.example.storemate.domain.model.TransactionListIntent
import com.example.storemate.domain.model.TransactionListScreenState
import com.example.storemate.domain.repositories.InventoryRepository
import com.example.storemate.presentation.UiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class TransactionListViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<TransactionListScreenState>>(UiState.Loading)
    val uiState: StateFlow<UiState<TransactionListScreenState>> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<TransactionListEffect>()
    val effects: SharedFlow<TransactionListEffect> = _effects.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedProductId = MutableStateFlow<Int?>(null)
    private val _selectedType = MutableStateFlow<String?>(null)

    init {
        loadInitialData()
        observeSearchAndFilters()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchAndFilters() {
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300),
                _selectedProductId,
                _selectedType,
                repository.getAllTransactionsFlow()
            ) { query, productId, type, transactions ->
                Triple(query, productId, type) to transactions
            }.catch {
                _uiState.value = UiState.Error("Filtering failed: ${it.message}")
            }.collectLatest { (filters, transactions) ->
                val (query, productId, type) = filters

                val filtered = transactions.filter {
                    val matchQuery = query.isBlank() || it.notes?.contains(query, true) == true
                    val matchProduct = productId == null || it.productId == productId
                    val matchType = type == null || it.type.equals(type, true)
                    matchQuery && matchProduct && matchType
                }

                val currentState =
                    (_uiState.value as? UiState.Success)?.data ?: return@collectLatest

                val productNames = repository.getAllProducts().associateBy { it.id }
                    .map { it.key to it.value.name }
                val allTypes = transactions.map { it.type }.distinct()

                _uiState.value = UiState.Success(
                    currentState.copy(
                        transactions = filtered,
                        searchQuery = query,
                        selectedProductId = productId,
                        selectedType = type,
                        productOptions = productNames,
                        typeOptions = allTypes
                    )
                )
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val transactions = repository.getAllTransactions()
                val productNames = repository.getAllProducts().associateBy { it.id }
                    .map { it.key to it.value.name }
                val allTypes = transactions.map { it.type }.distinct()

                _uiState.value = UiState.Success(
                    TransactionListScreenState(
                        transactions = transactions,
                        productOptions = productNames,
                        typeOptions = allTypes
                    )
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load transactions: ${e.message}")
            }
        }
    }

    fun onIntent(intent: TransactionListIntent) {
        when (intent) {
            is TransactionListIntent.SearchChanged -> _searchQuery.value = intent.query
            is TransactionListIntent.ProductFilterChanged -> _selectedProductId.value =
                intent.productId

            is TransactionListIntent.TypeFilterChanged -> _selectedType.value = intent.type
            is TransactionListIntent.ClearFilters -> {
                _searchQuery.value = ""
                _selectedProductId.value = null
                _selectedType.value = null
            }

            TransactionListIntent.NavigateToAddTransaction -> {
                viewModelScope.launch {
                    _effects.emit(TransactionListEffect.NavigateToAddTransaction)
                }
            }
        }
    }
}
