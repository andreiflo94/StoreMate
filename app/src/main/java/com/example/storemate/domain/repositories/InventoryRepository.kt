package com.example.storemate.domain.repositories

import com.example.storemate.domain.model.Product
import com.example.storemate.domain.model.Supplier
import com.example.storemate.domain.model.Transaction
import com.example.storemate.domain.model.TransactionWithProductName
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {

    // Products
    fun getAllProductsFlow(): Flow<List<Product>>
    suspend fun getAllProducts(): List<Product>
    suspend fun getProductById(id: Int): Product?
    suspend fun insertProduct(product: Product): Long
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
    fun getLowStockProductsFlow(): Flow<List<Product>>
    suspend fun getLowStockProducts(): List<Product>
    suspend fun searchProducts(query: String): List<Product>
    suspend fun filterProductsByCategory(category: String): List<Product>
    suspend fun filterProductsBySupplier(supplierId: Int): List<Product>

    // Suppliers
    fun getAllSuppliersFlow(): Flow<List<Supplier>>
    suspend fun getAllSuppliers(): List<Supplier>
    suspend fun getSupplierById(id: Int): Supplier?
    suspend fun insertSupplier(supplier: Supplier): Long
    suspend fun updateSupplier(supplier: Supplier)
    suspend fun deleteSupplier(supplier: Supplier)
    suspend fun searchSuppliers(query: String): List<Supplier>

    // Transactions
    fun getAllTransactionsFlow(): Flow<List<Transaction>>
    suspend fun getAllTransactions(): List<Transaction>
    fun getRecentTransactionsFlow(limit: Int): Flow<List<Transaction>>
    suspend fun getRecentTransactions(limit: Int): List<Transaction>
    suspend fun getTransactionsByType(type: String): List<Transaction>
    suspend fun getTransactionsByProduct(productId: Int): List<Transaction>
    suspend fun filterTransactionsByDateRange(from: Long, to: Long): List<Transaction>
    suspend fun filterTransactionsByTypeAndProduct(type: String, productId: Int): List<Transaction>
    suspend fun insertTransaction(transaction: Transaction): Long
    fun getTransactionsWithProductNameFlow(): Flow<List<TransactionWithProductName>>
    fun getRecentTransactionsWithProductNameFlow(limit: Int): Flow<List<TransactionWithProductName>>
}
