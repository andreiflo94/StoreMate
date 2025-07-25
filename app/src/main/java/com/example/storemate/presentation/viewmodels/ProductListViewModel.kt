package com.example.storemate.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storemate.domain.model.ProductListEffect
import com.example.storemate.domain.model.ProductListIntent
import com.example.storemate.domain.model.ProductListScreenState
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

    private fun observeSearchAndFilters() {
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300),
                _selectedCategory,
                _selectedSupplierId,
                repository.getAllProductsFlow()
            ) { query, category, supplierId, products ->
                val filtered = products.filter { product ->
                    val matchesQuery = query.isBlank() || product.name.contains(query, ignoreCase = true)
                    val matchesCategory = category == null || product.category == category
                    val matchesSupplier = supplierId == null || product.supplierId == supplierId.toInt()
                    matchesQuery && matchesCategory && matchesSupplier
                }

                val currentState = (_uiState.value as? UiState.Success)?.data
                if (currentState != null) {
                    _uiState.value = UiState.Success(
                        currentState.copy(
                            products = filtered,
                            searchQuery = query,
                            selectedCategory = category,
                            selectedSupplierId = supplierId
                        )
                    )
                }
            }.catch {
                _uiState.value = UiState.Error("Filtering failed: ${it.message}")
            }.collect{

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
                        suppliers = suppliers,
                    )
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load data: ${e.message}")
            }
        }
    }

    fun onIntent(action: ProductListIntent) {
        when (action) {
            is ProductListIntent.SearchChanged -> {
                _searchQuery.value = action.query
            }

            is ProductListIntent.CategorySelected -> {
                _selectedCategory.value = action.category
            }

            is ProductListIntent.SupplierSelected -> {
                _selectedSupplierId.value = action.supplierId
            }

            is ProductListIntent.DeleteProduct -> {
                viewModelScope.launch {
                    try {
                        repository.deleteProduct(action.product)
                        _effects.emit(ProductListEffect.ShowMessageToUi("Product deleted"))
                    } catch (e: Exception) {
                        _effects.emit(ProductListEffect.ShowErrorToUi("Failed to delete product: ${e.message}"))
                    }
                }
            }

            ProductListIntent.ClearFilters -> {
                _searchQuery.value = ""
                _selectedCategory.value = null
                _selectedSupplierId.value = null
            }

            is ProductListIntent.ProductClicked -> {
                viewModelScope.launch {
                    _effects.emit(ProductListEffect.NavigateToProductDetail(productId = action.productId))
                }
            }

            ProductListIntent.NavigateToAddProduct -> {
                viewModelScope.launch {
                    _effects.emit(ProductListEffect.NavigateToAddProduct)
                }
            }
        }
    }

}

