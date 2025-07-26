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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TransactionListViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<TransactionListScreenState>>(UiState.Loading)
    val uiState: StateFlow<UiState<TransactionListScreenState>> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<TransactionListEffect>()
    val effects: SharedFlow<TransactionListEffect> = _effects.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedType = MutableStateFlow<String?>(null)
    private val _sortAscending = MutableStateFlow(false)

    init {
        loadInitialData()
        observeSearchAndFilters()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchAndFilters() {
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300),
                _selectedType,
                _sortAscending,
                repository.getTransactionsWithProductNameFlow()
            ) { query, type, ascending, transactions ->
                Triple(query, type, ascending) to transactions
            }.catch {
                _uiState.value = UiState.Error("Filtering failed: ${it.message}")
            }.collectLatest { (filters, transactions) ->
                val (query, type, sortAscending) = filters

                val filtered = transactions.filter { t ->
                    val transaction = t.transaction
                    val matchQuery = query.isBlank()
                            || transaction.notes?.contains(query, ignoreCase = true) == true
                            || t.productName.contains(query, ignoreCase = true)

                    val matchType = type == null || transaction.type.equals(type, ignoreCase = true)

                    matchQuery && matchType
                }.let { list ->
                    if (sortAscending) list.sortedBy { it.transaction.date }
                    else list.sortedByDescending { it.transaction.date }
                }

                val currentState =
                    (_uiState.value as? UiState.Success)?.data ?: return@collectLatest
                val allTypes = transactions.map { it.transaction.type }.distinct()

                _uiState.value = UiState.Success(
                    currentState.copy(
                        transactions = ArrayList(filtered),
                        searchQuery = query,
                        selectedType = type,
                        typeOptions = allTypes,
                        sortAscending = sortAscending
                    )
                )
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val transactions = repository.getTransactionsWithProductNameFlow().first()
                val sorted = transactions.sortedByDescending { it.transaction.date }
                val allTypes = transactions.map { it.transaction.type }.distinct()

                _uiState.value = UiState.Success(
                    TransactionListScreenState(
                        transactions = ArrayList(sorted),
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
            is TransactionListIntent.SortOrderChanged -> _sortAscending.value = intent.ascending
            is TransactionListIntent.TypeFilterChanged -> _selectedType.value = intent.type
            is TransactionListIntent.ClearFilters -> {
                _searchQuery.value = ""
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
