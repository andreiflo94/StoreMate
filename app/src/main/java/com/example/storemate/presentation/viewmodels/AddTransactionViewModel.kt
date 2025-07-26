package com.example.storemate.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storemate.domain.model.AddTransactionEffect
import com.example.storemate.domain.model.AddTransactionIntent
import com.example.storemate.domain.model.AddTransactionScreenState
import com.example.storemate.domain.model.Product
import com.example.storemate.domain.model.Transaction
import com.example.storemate.domain.model.TransactionType
import com.example.storemate.domain.repositories.InventoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddTransactionScreenState>(AddTransactionScreenState())
    val uiState: StateFlow<AddTransactionScreenState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<AddTransactionEffect>()
    val effects: SharedFlow<AddTransactionEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            val products = repository.getAllProducts()
            _uiState.update {
                it.copy(productOptions = products.map { p -> p.id to p.name })
            }
        }
    }

    fun onIntent(intent: AddTransactionIntent) {
        when (intent) {
            is AddTransactionIntent.ProductSelected -> {
                _uiState.update { it.copy(productId = intent.productId, validationError = null) }
            }

            is AddTransactionIntent.TypeSelected -> {
                _uiState.update { it.copy(type = intent.type, validationError = null) }
            }

            is AddTransactionIntent.QuantityChanged -> {
                _uiState.update { it.copy(quantity = intent.quantity, validationError = null) }
            }

            is AddTransactionIntent.NotesChanged -> {
                _uiState.update { it.copy(notes = intent.notes) }
            }

            AddTransactionIntent.SubmitTransaction -> {
                submit()
            }
        }
    }


    private fun submit() {
        val state = _uiState.value
        val quantityInt = state.quantity.toIntOrNull()

        if (!validateFields(state.productId, state.type, quantityInt)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            val product = repository.getProductById(state.productId!!)
            if (!validateProduct(product)) return@launch

            if (!validateStockAvailability(state.type!!, quantityInt!!, product!!)) return@launch

            try {
                val transaction = createTransaction(state, quantityInt)
                repository.insertTransaction(transaction)

                val newStockLevel = calculateNewStockLevel(state.type, quantityInt, product)
                    ?: return@launch

                repository.updateProduct(product.copy(currentStockLevel = newStockLevel))

                _effects.emit(AddTransactionEffect.TransactionSaved)
            } catch (e: Exception) {
                _effects.emit(AddTransactionEffect.ShowErrorToUi("Failed to save: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    private fun validateFields(productId: Int?, type: String?, quantity: Int?): Boolean {
        return if (productId == null || type.isNullOrBlank() || quantity == null || quantity <= 0) {
            _uiState.update {
                it.copy(validationError = "All fields must be filled and quantity must be > 0")
            }
            false
        } else true
    }

    private suspend fun validateProduct(product: Product?): Boolean {
        return if (product == null) {
            _effects.emit(AddTransactionEffect.ShowErrorToUi("Invalid product selected"))
            _uiState.update { it.copy(isSubmitting = false) }
            false
        } else true
    }

    private suspend fun validateStockAvailability(
        type: String,
        quantity: Int,
        product: Product
    ): Boolean {
        return if (type == TransactionType.sale.toString() && product.currentStockLevel < quantity) {
            _effects.emit(AddTransactionEffect.ShowErrorToUi("Insufficient stock"))
            _uiState.update { it.copy(isSubmitting = false) }
            false
        } else true
    }

    private fun createTransaction(state: AddTransactionScreenState, quantity: Int): Transaction {
        return Transaction(
            date = System.currentTimeMillis(),
            type = state.type!!,
            productId = state.productId!!,
            quantity = quantity,
            notes = state.notes
        )
    }

    private suspend fun calculateNewStockLevel(
        type: String,
        quantity: Int,
        product: Product
    ): Int? {
        return when (type) {
            TransactionType.sale.toString() -> product.currentStockLevel - quantity
            TransactionType.restock.toString() -> product.currentStockLevel + quantity
            else -> {
                _effects.emit(AddTransactionEffect.ShowErrorToUi("Invalid transaction type"))
                _uiState.update { it.copy(isSubmitting = false) }
                null
            }
        }
    }

}
