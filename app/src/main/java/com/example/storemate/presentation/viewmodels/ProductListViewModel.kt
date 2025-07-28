package com.example.storemate.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storemate.domain.model.ProductListEffect
import com.example.storemate.domain.model.ProductListIntent
import com.example.storemate.domain.model.ProductListScreenState
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

class ProductListViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ProductListScreenState>>(UiState.Loading)
    val uiState: StateFlow<UiState<ProductListScreenState>> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ProductListEffect>()
    val effects: SharedFlow<ProductListEffect> = _effects.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _selectedSupplierId = MutableStateFlow<Int?>(null)

    init {
        loadInitialData()
        observeSearchAndFilters()
    }

    fun onIntent(intent: ProductListIntent) {
        when (intent) {
            is ProductListIntent.SearchChanged -> _searchQuery.value = intent.query
            is ProductListIntent.CategorySelected -> _selectedCategory.value = intent.category
            is ProductListIntent.SupplierSelected -> _selectedSupplierId.value = intent.supplierId
            is ProductListIntent.DeleteProduct -> handleDeleteProduct(intent)
            ProductListIntent.ClearFilters -> clearFilters()
            is ProductListIntent.ProductClicked -> emitEffect(
                ProductListEffect.NavigateToProductDetail(
                    intent.productId
                )
            )

            ProductListIntent.NavigateToAddProduct -> emitEffect(ProductListEffect.NavigateToAddProduct)
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchAndFilters() {
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300),
                _selectedCategory,
                _selectedSupplierId,
                repository.getAllProductsFlow()
            ) { query, category, supplierId, products ->
                Triple(query, category, supplierId) to products
            }.catch { throwable ->
                emitEffect(ProductListEffect.ShowErrorToUi("Filtering failed: ${throwable.message}"))
            }.collectLatest { (filters, products) ->
                val (query, category, supplierId) = filters

                val filtered = products.filter { product ->
                    val matchesQuery =
                        query.isBlank() || product.name.contains(query, ignoreCase = true)
                    val matchesCategory = category == null || product.category == category
                    val matchesSupplier = supplierId == null || product.supplierId == supplierId
                    matchesQuery && matchesCategory && matchesSupplier
                }

                val categories = products.map { it.category }.distinct()
                val suppliers = repository.getAllSuppliers().map { it.id to it.name }

                updateState { current ->
                    current.copy(
                        products = filtered,
                        searchQuery = query,
                        selectedCategory = category,
                        selectedSupplierId = supplierId,
                        categories = categories,
                        suppliers = suppliers
                    )
                }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val products = repository.getAllProducts()
                val categories = products.map { it.category }.distinct()
                val suppliers = repository.getAllSuppliers().map { it.id to it.name }

                _uiState.value = UiState.Success(
                    ProductListScreenState(
                        products = products,
                        categories = categories,
                        suppliers = suppliers
                    )
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load data: ${e.message}")
            }
        }
    }

    private fun updateState(reducer: (ProductListScreenState) -> ProductListScreenState) {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(reducer(currentState))
    }

    private fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _selectedSupplierId.value = null
    }

    private fun handleDeleteProduct(intent: ProductListIntent.DeleteProduct) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(intent.product)
                emitEffect(ProductListEffect.ShowMessageToUi("Product deleted"))
            } catch (e: Exception) {
                emitEffect(ProductListEffect.ShowErrorToUi("Failed to delete product: ${e.message}"))
            }
        }
    }

    private fun emitEffect(effect: ProductListEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}
