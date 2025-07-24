package com.example.storemate.domain.model

data class Product(
    val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val barcode: String,
    val supplierId: Int,
    val currentStockLevel: Int,
    val minimumStockLevel: Int
)

data class Supplier(
    val id: Int = 0,
    val name: String,
    val contactPerson: String,
    val phone: String,
    val email: String,
    val address: String
)

data class Transaction(
    val id: Int = 0,
    val date: Long,
    val type: String,
    val productId: Int,
    val quantity: Int,
    val notes: String?
)
