package com.example.storemate.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// Auth
// ---------------------------------------------------------------------------

@Serializable
data class AuthRequestDto(
    val username: String,
    val password: String
)

@Serializable
data class AuthResponseDto(
    val token: String,
    val username: String,
    val storeName: String,
    val expiresInSeconds: Long
)

@Serializable
data class CurrentUserDto(
    val username: String,
    val storeId: Long,
    val storeName: String
)

// ---------------------------------------------------------------------------
// Inventory
// ---------------------------------------------------------------------------

@Serializable
data class SupplierDto(
    val id: Long = 0,
    val name: String,
    val contactPerson: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = ""
)

@Serializable
data class ProductDto(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val barcode: String = "",
    val supplierId: Long? = null,
    val supplierName: String? = null,
    val currentStockLevel: Int = 0,
    val minimumStockLevel: Int = 0
)

@Serializable
data class TransactionDto(
    val id: Long = 0,
    /** ISO-8601 local date-time as produced by the server, e.g. "2026-07-26T11:38:46.699885". */
    val date: String,
    val type: String,
    val productId: Long,
    val productName: String = "",
    val quantity: Int,
    val notes: String? = null
)

/** Write payload for a transaction — the server assigns the id and moves stock. */
@Serializable
data class CreateTransactionDto(
    val date: String,
    val type: String,
    val productId: Long,
    val quantity: Int,
    val notes: String? = null
)

// ---------------------------------------------------------------------------
// Envelopes
// ---------------------------------------------------------------------------

@Serializable
data class PageDto<T>(
    val content: List<T> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)

/** Error body returned by the backend's `GlobalExceptionHandler`. */
@Serializable
data class ApiErrorDto(
    val status: Int = 0,
    val error: String = "",
    @SerialName("message") val message: String = "",
    val fieldErrors: Map<String, String> = emptyMap()
)
