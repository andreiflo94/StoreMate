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
import com.example.storemate.presentation.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<AddTransactionScreenState>>(UiState.Loading)
    val uiState: StateFlow<UiState<AddTransactionScreenState>> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<AddTransactionEffect>()
    val effects: SharedFlow<AddTransactionEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            val products = repository.getAllProducts()
            val options = products.map { it.id to it.name }
            updateState { it.copy(productOptions = options) }
        }
    }

    fun onIntent(intent: AddTransactionIntent) {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: AddTransactionScreenState()

        when (intent) {
            is AddTransactionIntent.ProductSelected -> updateState {
                it.copy(productId = intent.productId)
            }

            is AddTransactionIntent.TypeSelected -> updateState {
                it.copy(type = intent.type)
            }

            is AddTransactionIntent.QuantityChanged -> updateState {
                it.copy(quantity = intent.quantity)
            }

            is AddTransactionIntent.NotesChanged -> updateState {
                it.copy(notes = intent.notes)
            }

            AddTransactionIntent.SubmitTransaction -> submit(currentState)
        }
    }

    private fun submit(state: AddTransactionScreenState) {
        viewModelScope.launch {

            val quantityInt = state.quantity.toIntOrNull()

            if (!validateFields(state.productId, state.type, quantityInt)) return@launch

            updateState { it.copy(isSubmitting = true) }

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
                updateState { it.copy(isSubmitting = false) }
            }
        }
    }

    private fun updateState(transform: (AddTransactionScreenState) -> AddTransactionScreenState) {
        val current = (_uiState.value as? UiState.Success)?.data ?: AddTransactionScreenState()
        _uiState.value = UiState.Success(transform(current))
    }

    private suspend fun validateFields(productId: Int?, type: String?, quantity: Int?): Boolean {
        return if (productId == null || type.isNullOrBlank() || quantity == null || quantity <= 0) {
            _effects.emit(AddTransactionEffect.ShowErrorToUi("Invalid product selected"))
            updateState { it.copy(isSubmitting = false) }
            false
        } else true
    }

    private suspend fun validateProduct(product: Product?): Boolean {
        return if (product == null) {
            _effects.emit(AddTransactionEffect.ShowErrorToUi("Invalid product selected"))
            updateState { it.copy(isSubmitting = false) }
            false
        } else true
    }

    private suspend fun validateStockAvailability(
        type: String,
        quantity: Int,
        product: Product
    ): Boolean {
        return if (type == TransactionType.sale.toString() && product.currentStockLevel < quantity) {
            _effects.emit(AddTransactionEffect.ShowErrorToUi("Fail: Insufficient stock"))
            updateState { it.copy(isSubmitting = false) }
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
                updateState { it.copy(isSubmitting = false) }
                null
            }
        }
    }
}
