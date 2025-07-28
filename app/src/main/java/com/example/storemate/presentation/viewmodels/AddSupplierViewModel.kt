package com.example.storemate.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storemate.common.isValidEmail
import com.example.storemate.common.isValidPhoneNumber
import com.example.storemate.domain.model.AddSupplierEffect
import com.example.storemate.domain.model.AddSupplierIntent
import com.example.storemate.domain.model.AddSupplierScreenState
import com.example.storemate.domain.model.Supplier
import com.example.storemate.domain.repositories.InventoryRepository
import com.example.storemate.presentation.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddSupplierViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<UiState<AddSupplierScreenState>>(UiState.Success(AddSupplierScreenState()))
    val uiState = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<AddSupplierEffect>()
    val effects = _effects.asSharedFlow()

    init {
        if (savedStateHandle.contains("supplierId")) {
            savedStateHandle.get<Int>("supplierId")?.let { supplierId ->
                if (supplierId != -1) {
                    loadSupplier(supplierId)
                }
            }
        }
    }

    fun onIntent(intent: AddSupplierIntent) {
        when (intent) {
            is AddSupplierIntent.NameChanged -> updateState { it.copy(name = intent.name) }
            is AddSupplierIntent.ContactPersonChanged -> updateState { it.copy(contactPerson = intent.contactPerson) }
            is AddSupplierIntent.PhoneChanged -> updateState { it.copy(phone = intent.phone) }
            is AddSupplierIntent.EmailChanged -> updateState { it.copy(email = intent.email) }
            is AddSupplierIntent.AddressChanged -> updateState { it.copy(address = intent.address) }
            AddSupplierIntent.SaveSupplier -> saveSupplier()
        }
    }

    private fun loadSupplier(supplierId: Int) {
        viewModelScope.launch {
            repository.getSupplierById(supplierId)?.let { supplier ->
                updateState { currentState ->
                    currentState.copy(
                        screenTitle = "Edit supplier",
                        name = supplier.name,
                        contactPerson = supplier.contactPerson,
                        phone = supplier.phone,
                        email = supplier.email,
                        address = supplier.address,
                        isSaving = false,
                        supplierIdToEdit = supplierId
                    )
                }
            }
        }
    }

    private fun updateState(reducer: (AddSupplierScreenState) -> AddSupplierScreenState) {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: AddSupplierScreenState()
        _uiState.value = UiState.Success(reducer(currentState))
    }

    private fun saveSupplier() {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return

        // Validate required fields
        if (currentState.name.isBlank()) {
            showError("Supplier name is required.")
            return
        }
        if (!currentState.phone.isValidPhoneNumber()) {
            showError("Valid phone is required.")
            return
        }
        if (currentState.contactPerson.isBlank()) {
            showError("Contact person is required.")
            return
        }
        if (!currentState.email.isValidEmail()) {
            showError("Valid email is required.")
            return
        }
        if (currentState.address.isBlank()) {
            showError("Address is required.")
            return
        }
        // Other validations can be added here

        updateState { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val supplier = Supplier(
                    id = currentState.supplierIdToEdit ?: 0,
                    name = currentState.name,
                    contactPerson = currentState.contactPerson,
                    phone = currentState.phone,
                    email = currentState.email,
                    address = currentState.address
                )
                currentState.supplierIdToEdit?.let {
                    repository.updateSupplier(supplier)
                } ?: run {
                    repository.insertSupplier(supplier)
                }
                updateState { it.copy(isSaving = false) }
                supplierSaved()
            } catch (e: Exception) {
                updateState { it.copy(isSaving = false) }
                showError("Failed to save supplier: ${e.message}")
            }
        }
    }

    private fun supplierSaved() {
        viewModelScope.launch {
            _effects.emit(AddSupplierEffect.SupplierSaved)
        }
    }

    private fun showError(message: String) {
        viewModelScope.launch {
            _effects.emit(AddSupplierEffect.ShowError(message))
        }
    }
}
