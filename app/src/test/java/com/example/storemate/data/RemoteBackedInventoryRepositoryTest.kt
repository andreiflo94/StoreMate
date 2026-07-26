package com.example.storemate.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.storemate.data.mapper.toEntity
import com.example.storemate.data.remote.InventoryRemoteDataSource
import com.example.storemate.data.remote.ServerUnreachableException
import com.example.storemate.data.repositories.InventoryRepositoryImpl
import com.example.storemate.data.repositories.RemoteBackedInventoryRepository
import com.example.storemate.domain.model.Product
import com.example.storemate.domain.model.Supplier
import com.example.storemate.domain.model.Transaction
import com.example.storemate.domain.repositories.SyncState
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.ConnectException

/**
 * The design promise of the read-through cache is that the local database can
 * never hold a change the server has not accepted. These tests pin that down
 * against a real in-memory Room database, mocking only the network boundary —
 * mocking `db.withTransaction` itself hangs, since it schedules real work on
 * Room's transaction executor.
 */
@RunWith(RobolectricTestRunner::class)
class RemoteBackedInventoryRepositoryTest {

    private lateinit var db: StoreMateDb
    private lateinit var remote: InventoryRemoteDataSource
    private lateinit var repository: RemoteBackedInventoryRepository

    private val product = Product(
        id = 0,
        name = "Milk",
        description = "Fresh",
        price = 5.5,
        category = "Dairy",
        barcode = "123",
        supplierId = 0,
        currentStockLevel = 10,
        minimumStockLevel = 2
    )

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StoreMateDb::class.java
        ).build()
        remote = mockk(relaxed = true)
        repository = RemoteBackedInventoryRepository(InventoryRepositoryImpl(db), remote, db)
    }

    @After
    fun tearDown() {
        clearAllMocks()
        db.close()
    }

    @Test
    fun `insert returns the server assigned id and caches the server's version`() = runTest {
        val saved = product.copy(id = 42, name = "Milk")
        coEvery { remote.createProduct(product) } returns saved

        val id = repository.insertProduct(product)

        assertEquals(42L, id)
        assertEquals("Milk", db.productDao().getById(42)?.name)
    }

    @Test
    fun `a failed write leaves the cache untouched`() = runTest {
        coEvery { remote.createProduct(any()) } throws ConnectException("no route")

        val thrown = runCatching { repository.insertProduct(product) }.exceptionOrNull()

        assertTrue(thrown is ServerUnreachableException)
        assertTrue(db.productDao().getAll().isEmpty())
    }

    @Test
    fun `recording a transaction refreshes the product whose stock the server moved`() = runTest {
        // Seed the product the transaction refers to, as an earlier sync would have.
        db.productDao().insert(product.copy(id = 7).toEntity())

        val transaction = Transaction(
            id = 0, date = 0L, type = "sale", productId = 7, quantity = 3, notes = null
        )
        coEvery { remote.createTransaction(transaction) } returns transaction.copy(id = 99)
        coEvery { remote.fetchProduct(7) } returns product.copy(id = 7, currentStockLevel = 7)

        val id = repository.insertTransaction(transaction)

        assertEquals(99L, id)
        assertEquals(7, db.productDao().getById(7)?.currentStockLevel)
        assertEquals(1, db.transactionDao().getAll().size)
    }

    @Test
    fun `a failed sync reports offline but preserves the existing cache`() = runTest {
        // Populate the cache directly, as an earlier successful sync would have.
        db.supplierDao().insert(Supplier(1, "S", "", "", "", "").toEntity())
        coEvery { remote.fetchAllSuppliers() } throws ConnectException("server down")

        val result = repository.refreshAll()

        assertTrue(result.isFailure)
        assertTrue(repository.syncState.value is SyncState.Failed)
        assertEquals(1, db.supplierDao().getAll().size)
    }

    @Test
    fun `a successful sync marks the cache as synced`() = runTest {
        coEvery { remote.fetchAllSuppliers() } returns emptyList()
        coEvery { remote.fetchAllProducts() } returns emptyList()
        coEvery { remote.fetchAllTransactions() } returns emptyList()

        assertTrue(repository.refreshAll().isSuccess)
        assertTrue(repository.syncState.value is SyncState.Synced)
    }
}
