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

    fun onIntent(intent: SupplierListIntent) {
        when (intent) {
            is SupplierListIntent.SearchChanged -> _searchQuery.value = intent.query
            is SupplierListIntent.DeleteSupplier -> handleDeleteSupplier(intent)
            is SupplierListIntent.SupplierClicked -> emitEffect(
                SupplierListEffect.NavigateToSupplierDetail(
                    intent.supplierId
                )
            )

            SupplierListIntent.NavigateToAddSupplier -> emitEffect(SupplierListEffect.NavigateToAddSupplier)
            SupplierListIntent.ClearSearch -> _searchQuery.value = ""
        }
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
                    emitEffect(SupplierListEffect.ShowErrorToUi("Filtering failed: ${throwable.message}"))
                }
                .collectLatest { (query, suppliers) ->
                    val filtered = suppliers.filter { supplier ->
                        query.isBlank() || supplier.name.contains(query, ignoreCase = true)
                    }

                    updateState { current ->
                        current.copy(
                            suppliers = filtered,
                            searchQuery = query
                        )
                    }
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

    private fun updateState(reducer: (SupplierListScreenState) -> SupplierListScreenState) {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(reducer(currentState))
    }

    private fun handleDeleteSupplier(intent: SupplierListIntent.DeleteSupplier) {
        viewModelScope.launch {
            try {
                repository.deleteSupplier(intent.supplier)
                emitEffect(SupplierListEffect.ShowMessageToUi("Supplier deleted"))
            } catch (e: Exception) {
                val errorMessage = if (e.message?.contains("constraint") == true) {
                    "Failed to delete supplier, first delete supplier's products"
                } else {
                    "Failed to delete supplier: ${e.message}"
                }
                emitEffect(SupplierListEffect.ShowErrorToUi(errorMessage))
            }
        }
    }

    private fun emitEffect(effect: SupplierListEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}
