package com.heixss.storematerest.model

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import java.time.LocalDateTime

// ---------------------------------------------------------------------------
// Requests
// ---------------------------------------------------------------------------

data class SupplierDTO(
    @field:NotBlank(message = "Supplier name is required")
    val name: String,
    val contactPerson: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = ""
)

data class ProductDTO(
    @field:NotBlank(message = "Product name is required")
    val name: String,
    val description: String = "",
    @field:PositiveOrZero(message = "Price cannot be negative")
    val price: Double = 0.0,
    val category: String = "",
    val barcode: String = "",
    val supplierId: Long? = null,
    @field:PositiveOrZero(message = "Stock level cannot be negative")
    val currentStockLevel: Int = 0,
    @field:PositiveOrZero(message = "Minimum stock level cannot be negative")
    val minimumStockLevel: Int = 0
)

data class TransactionDTO(
    /** ISO-8601 local date-time, e.g. "2025-09-27T14:00:00". Defaults to now. */
    val date: String? = null,
    /** "RESTOCK" or "SALE" (case-insensitive). */
    @field:NotBlank(message = "Transaction type is required")
    val type: String,
    val productId: Long,
    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int,
    val notes: String? = null
)

data class AuthRequest(
    @field:NotBlank(message = "Username is required")
    val username: String,
    @field:NotBlank(message = "Password is required")
    val password: String
)

// ---------------------------------------------------------------------------
// Responses
//
// The API returns these instead of JPA entities so the wire format stays
// stable, never leaks the owning Store, and never trips over lazy proxies.
// ---------------------------------------------------------------------------

data class SupplierResponse(
    val id: Long,
    val name: String,
    val contactPerson: String,
    val phone: String,
    val email: String,
    val address: String
)

data class ProductResponse(
    val id: Long,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val barcode: String,
    val supplierId: Long?,
    val supplierName: String?,
    val currentStockLevel: Int,
    val minimumStockLevel: Int
)

data class TransactionResponse(
    val id: Long,
    val date: LocalDateTime,
    val type: String,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val notes: String?
)

data class AuthResponse(
    val token: String,
    val username: String,
    val storeName: String,
    val expiresInSeconds: Long
)

data class CurrentUserResponse(
    val username: String,
    val storeId: Long,
    val storeName: String
)

/**
 * Stable page envelope. Spring's own `Page` serialization is version-dependent,
 * so the client pages through this shape instead.
 */
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean
)

data class ApiError(
    val status: Int,
    val error: String,
    val message: String,
    val fieldErrors: Map<String, String> = emptyMap()
)
