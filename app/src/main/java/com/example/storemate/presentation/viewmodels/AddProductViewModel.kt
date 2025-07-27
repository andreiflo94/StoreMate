package com.example.storemate.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storemate.common.BarcodeScanner
import com.example.storemate.domain.model.AddProductEffect
import com.example.storemate.domain.model.AddProductIntent
import com.example.storemate.domain.model.AddProductScreenState
import com.example.storemate.domain.model.Product
import com.example.storemate.domain.model.Supplier
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

    private var currentState = AddProductScreenState()
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

    private fun loadProduct(productId: Int) {
        viewModelScope.launch {
            repository.getProductById(productId)?.let { product ->
                repository.getSupplierById(product.supplierId)?.let {
                    updateState(
                        title = "Edit product",
                        name = product.name,
                        description = product.description,
                        price = product.price.toString(),
                        category = product.category,
                        barcode = product.barcode,
                        supplierId = it.id,
                        currentStockLevel = product.currentStockLevel.toString(),
                        minimumStockLevel = product.minimumStockLevel.toString(),
                        suppliers = repository.getAllSuppliers(),
                        isSaving = false
                    )
                    productIdToEdit = productId
                }
            }
        }
    }

    private fun observeBarcodeScan() {
        viewModelScope.launch {
            barcodeScanner.barCodeResults.collect { barcode ->
                barcode?.let {
                    updateState(
                        barcode = barcode
                    )
                }
            }
        }
    }

    private fun observeSuppliers() {
        viewModelScope.launch {
            repository.getAllSuppliersFlow()
                .catch { e ->
                    _uiState.value = UiState.Error("Failed to load suppliers")
                }
                .collect { suppliers ->
                    currentState = currentState.copy(suppliers = suppliers)
                    _uiState.value = UiState.Success(currentState)
                }
        }
    }

    fun onIntent(intent: AddProductIntent) {
        when (intent) {
            is AddProductIntent.NameChanged -> updateState(name = intent.name)
            is AddProductIntent.DescriptionChanged -> updateState(description = intent.description)
            is AddProductIntent.PriceChanged -> updateState(price = intent.price)
            is AddProductIntent.CategoryChanged -> updateState(category = intent.category)
            is AddProductIntent.BarcodeChanged -> updateState(barcode = intent.barcode)
            is AddProductIntent.SupplierSelected -> updateState(supplierId = intent.supplierId)
            is AddProductIntent.CurrentStockLevelChanged -> updateState(currentStockLevel = intent.level)
            is AddProductIntent.MinimumStockLevelChanged -> updateState(minimumStockLevel = intent.level)
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

    private fun updateState(
        title: String = currentState.screenTitle,
        name: String = currentState.name,
        description: String = currentState.description,
        price: String = currentState.price,
        category: String = currentState.category,
        barcode: String = currentState.barcode,
        supplierId: Int? = currentState.supplierId,
        currentStockLevel: String = currentState.currentStockLevel,
        minimumStockLevel: String = currentState.minimumStockLevel,
        isSaving: Boolean = currentState.isSaving,
        suppliers: List<Supplier> = currentState.suppliers
    ) {
        currentState = AddProductScreenState(
            title,
            name,
            description,
            price,
            category,
            barcode,
            supplierId,
            currentStockLevel,
            minimumStockLevel,
            suppliers,
            isSaving
        )
        _uiState.value = UiState.Success(currentState)
    }

    private fun saveProduct() {
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
        updateState(isSaving = true)

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
                updateState(isSaving = false)
                productSaved()
            } catch (e: Exception) {
                updateState(isSaving = false)
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
