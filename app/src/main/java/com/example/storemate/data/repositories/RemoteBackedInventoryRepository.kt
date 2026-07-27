package com.example.storemate.data.repositories

import androidx.room.withTransaction
import com.example.storemate.data.StoreMateDb
import com.example.storemate.data.mapper.toEntity
import com.example.storemate.data.remote.InventoryRemoteDataSource
import com.example.storemate.data.remote.toUserFacingException
import com.example.storemate.domain.model.Product
import com.example.storemate.domain.model.Supplier
import com.example.storemate.domain.model.Transaction
import com.example.storemate.domain.model.TransactionWithProductName
import com.example.storemate.domain.repositories.InventoryRepository
import com.example.storemate.domain.repositories.SyncRepository
import com.example.storemate.domain.repositories.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Makes the shop's on-prem server the source of truth while keeping the local
 * Room database as a read-through cache.
 *
 *  - **Reads** are served from [local], so the app keeps rendering the last
 *    known inventory when the server or the Wi-Fi is down.
 *  - **Writes** go to the server first and are only mirrored into the cache
 *    once the server has accepted them. A write made while offline fails
 *    loudly rather than silently diverging from the server.
 *
 * This ordering is what removes the need for conflict resolution: the cache can
 * never hold a change the server has not already seen.
 */
class RemoteBackedInventoryRepository(
    private val local: InventoryRepository,
    private val remote: InventoryRemoteDataSource,
    private val db: StoreMateDb
) : InventoryRepository, SyncRepository {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    // -----------------------------------------------------------------------
    // Sync
    // -----------------------------------------------------------------------

    override suspend fun refreshAll(): Result<Unit> {
        _syncState.value = SyncState.Syncing
        return try {
            // Fetched before the transaction so a network failure leaves the
            // existing cache untouched.
            val suppliers = remote.fetchAllSuppliers()
            val products = remote.fetchAllProducts()
            val transactions = remote.fetchAllTransactions()

            replaceCache(suppliers, products, transactions)

            _syncState.value = SyncState.Synced(System.currentTimeMillis())
            Result.success(Unit)
        } catch (ex: Exception) {
            val mapped = ex.toUserFacingException()
            _syncState.value = SyncState.Failed(mapped.message ?: "Sync failed")
            Result.failure(mapped)
        }
    }

    private suspend fun replaceCache(
        suppliers: List<Supplier>,
        products: List<Product>,
        transactions: List<Transaction>
    ) = db.withTransaction {
        // Delete children first, insert parents first: transactions reference
        // products, products reference suppliers.
        db.transactionDao().deleteAll()
        db.productDao().deleteAll()
        db.supplierDao().deleteAll()

        // The three fetches are separate requests, so another till can create a
        // product in between and leave us holding a transaction for a product
        // this pass never saw. Dropping those rows costs one stale history entry
        // until the next sync; keeping them fails the whole sync on the foreign
        // key and leaves the cache empty.
        val knownProductIds = products.mapTo(mutableSetOf()) { it.id }

        db.supplierDao().insertAll(suppliers.map { it.toEntity() })
        db.productDao().insertAll(products.map { it.toEntity() })
        db.transactionDao().insertAll(
            transactions.filter { it.productId in knownProductIds }.map { it.toEntity() }
        )
    }

    // -----------------------------------------------------------------------
    // Products
    // -----------------------------------------------------------------------

    override fun getAllProductsFlow(): Flow<List<Product>> = local.getAllProductsFlow()
    override suspend fun getAllProducts(): List<Product> = local.getAllProducts()
    override suspend fun getProductById(id: Int): Product? = local.getProductById(id)
    override fun getLowStockProductsFlow(): Flow<List<Product>> = local.getLowStockProductsFlow()
    override suspend fun getLowStockProducts(): List<Product> = local.getLowStockProducts()
    override suspend fun searchProducts(query: String): List<Product> = local.searchProducts(query)

    override suspend fun filterProductsByCategory(category: String): List<Product> =
        local.filterProductsByCategory(category)

    override suspend fun filterProductsBySupplier(supplierId: Int): List<Product> =
        local.filterProductsBySupplier(supplierId)

    override suspend fun insertProduct(product: Product): Long = remoteFirst {
        val saved = remote.createProduct(product)
        cache(saved)
        saved.id.toLong()
    }

    override suspend fun updateProduct(product: Product) = remoteFirst {
        cache(remote.updateProduct(product))
    }

    override suspend fun deleteProduct(product: Product) = remoteFirst {
        remote.deleteProduct(product)
        // The local schema cascades the product's transactions away, matching
        // what the server just did.
        local.deleteProduct(product)
    }

    // -----------------------------------------------------------------------
    // Suppliers
    // -----------------------------------------------------------------------

    override fun getAllSuppliersFlow(): Flow<List<Supplier>> = local.getAllSuppliersFlow()
    override suspend fun getAllSuppliers(): List<Supplier> = local.getAllSuppliers()
    override suspend fun getSupplierById(id: Int): Supplier? = local.getSupplierById(id)
    override suspend fun searchSuppliers(query: String): List<Supplier> = local.searchSuppliers(query)

    override suspend fun insertSupplier(supplier: Supplier): Long = remoteFirst {
        val saved = remote.createSupplier(supplier)
        cache(saved)
        saved.id.toLong()
    }

    override suspend fun updateSupplier(supplier: Supplier) = remoteFirst {
        cache(remote.updateSupplier(supplier))
    }

    override suspend fun deleteSupplier(supplier: Supplier) = remoteFirst {
        remote.deleteSupplier(supplier)
        local.deleteSupplier(supplier)
        // The server detaches the supplier's products instead of deleting them,
        // so pull their new state rather than guessing at it.
        refreshProductsOnly()
    }

    // -----------------------------------------------------------------------
    // Transactions
    // -----------------------------------------------------------------------

    override fun getAllTransactionsFlow(): Flow<List<Transaction>> = local.getAllTransactionsFlow()
    override suspend fun getAllTransactions(): List<Transaction> = local.getAllTransactions()

    override fun getRecentTransactionsFlow(limit: Int): Flow<List<Transaction>> =
        local.getRecentTransactionsFlow(limit)

    override suspend fun getRecentTransactions(limit: Int): List<Transaction> =
        local.getRecentTransactions(limit)

    override suspend fun getTransactionsByType(type: String): List<Transaction> =
        local.getTransactionsByType(type)

    override suspend fun getTransactionsByProduct(productId: Int): List<Transaction> =
        local.getTransactionsByProduct(productId)

    override suspend fun filterTransactionsByDateRange(from: Long, to: Long): List<Transaction> =
        local.filterTransactionsByDateRange(from, to)

    override suspend fun filterTransactionsByTypeAndProduct(
        type: String,
        productId: Int
    ): List<Transaction> = local.filterTransactionsByTypeAndProduct(type, productId)

    override fun getTransactionsWithProductNameFlow(): Flow<List<TransactionWithProductName>> =
        local.getTransactionsWithProductNameFlow()

    override fun getRecentTransactionsWithProductNameFlow(limit: Int): Flow<List<TransactionWithProductName>> =
        local.getRecentTransactionsWithProductNameFlow(limit)

    override suspend fun insertTransaction(transaction: Transaction): Long = remoteFirst {
        val saved = remote.createTransaction(transaction)
        db.transactionDao().upsert(saved.toEntity())
        // Recording a transaction moves stock server-side, so the cached
        // product is now stale. Pull just that one back.
        runCatching { cache(remote.fetchProduct(saved.productId)) }
        saved.id.toLong()
    }

    /**
     * Upserts rather than replaces: a REPLACE insert deletes the existing row
     * first, which would cascade a product's cached transactions away.
     */
    private suspend fun cache(product: Product) = db.productDao().upsert(product.toEntity())

    private suspend fun cache(supplier: Supplier) = db.supplierDao().upsert(supplier.toEntity())

    private suspend fun refreshProductsOnly() {
        runCatching {
            // Upsert-only: clearing the table would cascade every cached
            // transaction away, and this path never deletes products anyway.
            db.productDao().upsertAll(remote.fetchAllProducts().map { it.toEntity() })
        }
    }

    /**
     * Runs a server write, translating failures into messages the UI can show.
     * Nothing touches the cache unless the server call succeeded.
     */
    private suspend fun <T> remoteFirst(block: suspend () -> T): T = try {
        block()
    } catch (ex: Exception) {
        throw ex.toUserFacingException()
    }
}
