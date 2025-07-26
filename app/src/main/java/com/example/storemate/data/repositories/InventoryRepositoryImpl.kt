package com.example.storemate.data.repositories

import com.example.storemate.data.StoreMateDb
import com.example.storemate.data.mapper.toDomain
import com.example.storemate.data.mapper.toEntity
import com.example.storemate.domain.model.Product
import com.example.storemate.domain.model.Supplier
import com.example.storemate.domain.model.Transaction
import com.example.storemate.domain.repositories.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InventoryRepositoryImpl(db: StoreMateDb) : InventoryRepository {

    private val productDao = db.productDao()
    private val supplierDao = db.supplierDao()
    private val transactionDao = db.transactionDao()

    //region Products
    override fun getAllProductsFlow(): Flow<List<Product>> =
        productDao.getAllFlow().map { it.map { productEntity -> productEntity.toDomain() } }

    override suspend fun getAllProducts(): List<Product> =
        productDao.getAll().map { it.toDomain() }

    override suspend fun getProductById(id: Int): Product? =
        productDao.getById(id)?.toDomain()

    override suspend fun insertProduct(product: Product) =
        productDao.insert(product.toEntity())

    override suspend fun updateProduct(product: Product) =
        productDao.update(product.toEntity())

    override suspend fun deleteProduct(product: Product) =
        productDao.delete(product.toEntity())

    override fun getLowStockProductsFlow(): Flow<List<Product>> =
        productDao.getLowStockFlow().map { it.map { it.toDomain() } }

    override suspend fun getLowStockProducts(): List<Product> =
        productDao.getLowStock().map { it.toDomain() }

    override suspend fun searchProducts(query: String): List<Product> =
        productDao.search(query).map { it.toDomain() }

    override suspend fun filterProductsByCategory(category: String): List<Product> =
        productDao.filterByCategory(category).map { it.toDomain() }

    override suspend fun filterProductsBySupplier(supplierId: Int): List<Product> =
        productDao.filterBySupplier(supplierId).map { it.toDomain() }
    //endregion

    //region Suppliers
    override fun getAllSuppliersFlow(): Flow<List<Supplier>> =
        supplierDao.getAllFlow().map { it.map { supplierEntity -> supplierEntity.toDomain() } }

    override suspend fun getAllSuppliers(): List<Supplier> =
        supplierDao.getAll().map { it.toDomain() }

    override suspend fun getSupplierById(id: Int): Supplier? =
        supplierDao.getById(id)?.toDomain()

    override suspend fun insertSupplier(supplier: Supplier) =
        supplierDao.insert(supplier.toEntity())

    override suspend fun updateSupplier(supplier: Supplier) =
        supplierDao.update(supplier.toEntity())

    override suspend fun deleteSupplier(supplier: Supplier) =
        supplierDao.delete(supplier.toEntity())

    override suspend fun searchSuppliers(query: String): List<Supplier> =
        supplierDao.search(query).map { it.toDomain() }
    //endregion

    //region Transactions
    override fun getAllTransactionsFlow(): Flow<List<Transaction>> =
        transactionDao.getAllFlow()
            .map { it.map { transactionEntity -> transactionEntity.toDomain() } }

    override suspend fun getAllTransactions(): List<Transaction> =
        transactionDao.getAll().map { it.toDomain() }

    override fun getRecentTransactionsFlow(limit: Int): Flow<List<Transaction>> =
        transactionDao.getRecentTransactionsFlow(limit).map { it.map { it.toDomain() } }

    override suspend fun getRecentTransactions(limit: Int): List<Transaction> =
        transactionDao.getRecentTransactions(limit).map { it.toDomain() }

    override suspend fun getTransactionsByType(type: String): List<Transaction> =
        transactionDao.getByType(type).map { it.toDomain() }

    override suspend fun getTransactionsByProduct(productId: Int): List<Transaction> =
        transactionDao.getByProduct(productId).map { it.toDomain() }

    override suspend fun filterTransactionsByDateRange(from: Long, to: Long): List<Transaction> =
        transactionDao.filterByDateRange(from, to).map { it.toDomain() }

    override suspend fun filterTransactionsByTypeAndProduct(
        type: String,
        productId: Int
    ): List<Transaction> =
        transactionDao.filterByTypeAndProduct(type, productId).map { it.toDomain() }

    override suspend fun insertTransaction(transaction: Transaction) =
        transactionDao.insert(transaction.toEntity())
    //endregion
}

