package com.example.storemate.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storemate.domain.model.SupplierListEffect
import com.example.storemate.domain.model.SupplierListIntent
import com.example.storemate.domain.model.SupplierListScreenState
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

class SupplierListViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<SupplierListScreenState>>(UiState.Loading)
    val uiState: StateFlow<UiState<SupplierListScreenState>> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<SupplierListEffect>()
    val effects: SharedFlow<SupplierListEffect> = _effects.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        loadInitialData()
        observeSearch()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .combine(repository.getAllSuppliersFlow()) { query, suppliers ->
                    query to suppliers
                }
                .catch { throwable ->
                    _uiState.value = UiState.Error("Filtering failed: ${throwable.message}")
                }
                .collectLatest { (query, suppliers) ->
                    val filtered = suppliers.filter { supplier ->
                        query.isBlank() || supplier.name.contains(query, ignoreCase = true)
                    }
                    val currentState =
                        (_uiState.value as? UiState.Success)?.data ?: SupplierListScreenState()

                    _uiState.value = UiState.Success(
                        currentState.copy(
                            suppliers = filtered,
                            searchQuery = query
                        )
                    )
                }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val suppliers = repository.getAllSuppliers()
                _uiState.value = UiState.Success(
                    SupplierListScreenState(
                        suppliers = suppliers,
                        searchQuery = ""
                    )
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load suppliers: ${e.message}")
            }
        }
    }

    fun onIntent(action: SupplierListIntent) {
        when (action) {
            is SupplierListIntent.SearchChanged -> {
                _searchQuery.value = action.query
            }

            is SupplierListIntent.DeleteSupplier -> {
                viewModelScope.launch {
                    try {
                        repository.deleteSupplier(action.supplier)
                        _effects.emit(SupplierListEffect.ShowMessageToUi("Supplier deleted"))
                    } catch (e: Exception) {
                        _effects.emit(SupplierListEffect.ShowErrorToUi("Failed to delete supplier: ${e.message}"))
                    }
                }
            }

            is SupplierListIntent.SupplierClicked -> {
                viewModelScope.launch {
                    _effects.emit(SupplierListEffect.NavigateToSupplierDetail(action.supplierId))
                }
            }

            SupplierListIntent.NavigateToAddSupplier -> {
                viewModelScope.launch {
                    _effects.emit(SupplierListEffect.NavigateToAddSupplier)
                }
            }

            SupplierListIntent.ClearSearch -> {
                _searchQuery.value = ""
            }
        }
    }
}
