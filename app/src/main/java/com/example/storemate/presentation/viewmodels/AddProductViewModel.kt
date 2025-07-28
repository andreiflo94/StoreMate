package com.example.storemate.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storemate.common.BarcodeScanner
import com.example.storemate.domain.model.AddProductEffect
import com.example.storemate.domain.model.AddProductIntent
import com.example.storemate.domain.model.AddProductScreenState
import com.example.storemate.domain.model.Product
import com.example.storemate.domain.repositories.InventoryRepository
import com.example.storemate.presentation.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AddProductViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: InventoryRepository,
    private val barcodeScanner: BarcodeScanner
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<AddProductScreenState>>(UiState.Loading)
    val uiState: StateFlow<UiState<AddProductScreenState>> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<AddProductEffect>()
    val effects = _effects.asSharedFlow()

    private var productIdToEdit: Int? = null

    init {
        if (savedStateHandle.contains("productId")) {
            savedStateHandle.get<Int>("productId")?.let { productId ->
                if (productId != 1) {
                    loadProduct(productId)
                }
            }
        }
        observeBarcodeScan()
        observeSuppliers()
    }

    fun onIntent(intent: AddProductIntent) {
        when (intent) {
            is AddProductIntent.NameChanged -> updateState {
                it.copy(name = intent.name)
            }

            is AddProductIntent.DescriptionChanged -> updateState {
                it.copy(description = intent.description)
            }

            is AddProductIntent.PriceChanged -> updateState {
                it.copy(price = intent.price)
            }

            is AddProductIntent.CategoryChanged -> updateState {
                it.copy(category = intent.category)
            }

            is AddProductIntent.BarcodeChanged -> updateState {
                it.copy(barcode = intent.barcode)
            }

            is AddProductIntent.SupplierSelected -> updateState {
                it.copy(supplierId = intent.supplierId)
            }

            is AddProductIntent.CurrentStockLevelChanged -> updateState {
                it.copy(currentStockLevel = intent.level)
            }

            is AddProductIntent.MinimumStockLevelChanged -> updateState {
                it.copy(minimumStockLevel = intent.level)
            }

            is AddProductIntent.NavigateToNewSupplier -> navigateToAddSupplier()
            is AddProductIntent.SaveProduct -> saveProduct()
            AddProductIntent.ScanBarcode -> {
                viewModelScope.launch {
                    try {
                        barcodeScanner.startScan()
                    } catch (e: Exception) {
                        if (e is CancellationException) {
                            return@launch
                        }
                        sendError("There was an error scanning the barcode")
                    }
                }
            }
        }
    }

    private fun loadProduct(productId: Int) {
        viewModelScope.launch {
            repository.getProductById(productId)?.let { product ->
                repository.getAllSuppliers().let { suppliers ->
                    updateState {
                        it.copy(
                            screenTitle = "Edit product",
                            name = product.name,
                            description = product.description,
                            price = product.price.toString(),
                            category = product.category,
                            barcode = product.barcode,
                            supplierId = product.supplierId,
                            currentStockLevel = product.currentStockLevel.toString(),
                            minimumStockLevel = product.minimumStockLevel.toString(),
                            suppliers = suppliers,
                            isSaving = false
                        )
                    }
                    productIdToEdit = productId
                }
            }
        }
    }

    private fun observeBarcodeScan() {
        viewModelScope.launch {
            barcodeScanner.barCodeResults.collect { barcode ->
                barcode?.let {
                    updateState {
                        it.copy(
                            barcode = barcode
                        )
                    }
                }
            }
        }
    }

    private fun observeSuppliers() {
        viewModelScope.launch {
            repository.getAllSuppliersFlow()
                .catch { _ ->
                    _uiState.value = UiState.Error("Failed to load suppliers")
                }
                .collect { suppliers ->
                    updateState {
                        it.copy(suppliers = suppliers)
                    }
                }
        }
    }

    private fun updateState(reducer: (AddProductScreenState) -> AddProductScreenState) {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: AddProductScreenState()
        _uiState.value = UiState.Success(reducer(currentState))
    }

    private fun saveProduct() {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: AddProductScreenState()
        val priceDouble = currentState.price.toDoubleOrNull()
        val currentStock = currentState.currentStockLevel.toIntOrNull() ?: 0
        val minimumStock = currentState.minimumStockLevel.toIntOrNull() ?: 0
        val supplierId = currentState.supplierId

        if (currentState.name.isBlank()) {
            sendError("Name is required")
            return
        }

        if (priceDouble == null || priceDouble <= 0) {
            sendError("Price must be a positive number")
            return
        }

        if (supplierId == null) {
            sendError("Supplier must be selected")
            return
        }

        if (!currentState.isValid()) {
            sendError("All fields must be completed")
            return
        }

        // Set loading state
        updateState {
            it.copy(isSaving = true)
        }

        viewModelScope.launch {
            try {
                val product = Product(
                    id = productIdToEdit ?: 0,
                    name = currentState.name.trim(),
                    description = currentState.description.trim(),
                    price = priceDouble,
                    category = currentState.category.trim(),
                    barcode = currentState.barcode.trim(),
                    supplierId = supplierId,
                    currentStockLevel = currentStock,
                    minimumStockLevel = minimumStock
                )
                productIdToEdit?.let {
                    repository.updateProduct(product)
                } ?: run {
                    repository.insertProduct(product)
                }
                // Set loading state
                updateState {
                    it.copy(isSaving = false)
                }
                productSaved()
            } catch (e: Exception) {
                updateState {
                    it.copy(isSaving = false)
                }
                sendError("Failed to save product: ${e.localizedMessage}")
            }
        }
    }

    private fun sendError(message: String) {
        viewModelScope.launch {
            _effects.emit(AddProductEffect.ShowError(message))
        }
    }

    private fun productSaved() {
        viewModelScope.launch {
            _effects.emit(AddProductEffect.ProductSaved)
        }
    }

    private fun navigateToAddSupplier() {
        viewModelScope.launch {
            _effects.emit(AddProductEffect.NavigateToAddSupplier)
        }
    }
}
