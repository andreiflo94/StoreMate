package com.example.storemate.repository

import com.example.storemate.data.StoreMateDb
import com.example.storemate.data.dao.ProductDao
import com.example.storemate.data.dao.SupplierDao
import com.example.storemate.data.dao.TransactionDao
import com.example.storemate.data.dbentities.ProductEntity
import com.example.storemate.data.dbentities.SupplierEntity
import com.example.storemate.data.dbentities.TransactionEntity
import com.example.storemate.data.repositories.InventoryRepositoryImpl
import com.example.storemate.domain.model.Product
import com.example.storemate.domain.model.Supplier
import com.example.storemate.domain.model.Transaction
import io.mockk.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class InventoryRepositoryImplTest {

    private lateinit var db: StoreMateDb
    private lateinit var productDao: ProductDao
    private lateinit var supplierDao: SupplierDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var repository: InventoryRepositoryImpl

    @Before
    fun setUp() {
        productDao = mockk(relaxed = true)
        supplierDao = mockk(relaxed = true)
        transactionDao = mockk(relaxed = true)
        db = mockk {
            every { productDao() } returns productDao
            every { supplierDao() } returns supplierDao
            every { transactionDao() } returns transactionDao
        }
        repository = InventoryRepositoryImpl(db)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    //region products
    @Test
    fun `getAllProducts returns mapped domain models`() = runTest {
        val entities = listOf(
            ProductEntity(1, "Milk", "Fresh milk", 5.5, "Dairy", "123456", 1, 20, 5),
            ProductEntity(2, "Bread", "Whole wheat", 3.0, "Bakery", "654321", 1, 10, 4)
        )
        coEvery { productDao.getAll() } returns entities

        val result = repository.getAllProducts()

        assertEquals(2, result.size)
        assertEquals("Milk", result[0].name)
        assertEquals("Bread", result[1].name)
        coVerify { productDao.getAll() }
    }

    @Test
    fun `getProductById returns product if found`() = runTest {
        val entity = ProductEntity(1, "Milk", "Fresh milk", 5.5, "Dairy", "123456", 1, 20, 5)
        coEvery { productDao.getById(1) } returns entity

        val result = repository.getProductById(1)

        assertNotNull(result)
        assertEquals("Milk", result?.name)
        coVerify { productDao.getById(1) }
    }

    @Test
    fun `getProductById returns null if not found`() = runTest {
        coEvery { productDao.getById(999) } returns null

        val result = repository.getProductById(999)

        assertNull(result)
        coVerify { productDao.getById(999) }
    }

    @Test
    fun `insertProduct delegates to DAO with entity`() = runTest {
        val product = Product(0, "Juice", "Orange", 4.0, "Drinks", "8888", 1, 12, 3)
        coEvery { productDao.insert(any()) } returns 10L

        repository.insertProduct(product)

        coVerify { productDao.insert(match { it.name == "Juice" && it.price == 4.0 }) }
    }

    @Test
    fun `updateProduct delegates to DAO with correct entity`() = runTest {
        val product = Product(5, "Updated", "Updated desc", 8.0, "Cat", "1234", 1, 9, 2)

        repository.updateProduct(product)

        coVerify { productDao.update(match { it.id == 5 && it.name == "Updated" }) }
    }

    @Test
    fun `deleteProduct delegates to DAO with correct entity`() = runTest {
        val product = Product(3, "ToDelete", "", 0.0, "", "", 1, 0, 0)

        repository.deleteProduct(product)

        coVerify { productDao.delete(match { it.id == 3 }) }
    }

    @Test
    fun `getLowStockProducts returns only low stock items`() = runTest {
        val entities = listOf(
            ProductEntity(1, "Item1", "", 1.0, "", "", 1, 2, 5)
        )
        coEvery { productDao.getLowStock() } returns entities

        val result = repository.getLowStockProducts()

        assertEquals(1, result.size)
        assertEquals("Item1", result[0].name)
    }

    @Test
    fun `searchProducts returns matching items`() = runTest {
        val entities = listOf(
            ProductEntity(2, "Apple Juice", "", 2.0, "Drinks", "juice123", 1, 10, 3)
        )
        coEvery { productDao.search("apple") } returns entities

        val result = repository.searchProducts("apple")

        assertEquals(1, result.size)
        assertEquals("Apple Juice", result[0].name)
    }

    @Test
    fun `filterProductsByCategory returns filtered list`() = runTest {
        val entities = listOf(
            ProductEntity(1, "Milk", "", 5.0, "Dairy", "", 1, 10, 2)
        )
        coEvery { productDao.filterByCategory("Dairy") } returns entities

        val result = repository.filterProductsByCategory("Dairy")

        assertEquals(1, result.size)
        assertEquals("Milk", result[0].name)
    }

    @Test
    fun `filterProductsBySupplier returns list for given supplierId`() = runTest {
        val entities = listOf(
            ProductEntity(10, "Coffee", "", 3.0, "Drinks", "", 99, 12, 4)
        )
        coEvery { productDao.filterBySupplier(99) } returns entities

        val result = repository.filterProductsBySupplier(99)

        assertEquals(1, result.size)
        assertEquals("Coffee", result[0].name)
    }
    //endregion

    //region suppliers
    @Test
    fun `getAllSuppliers returns mapped domain list`() = runTest {
        val entities = listOf(
            SupplierEntity(1, "Supplier A", "John Doe", "123456789", "a@example.com", "Address 1"),
            SupplierEntity(2, "Supplier B", "Jane Smith", "987654321", "b@example.com", "Address 2")
        )
        coEvery { supplierDao.getAll() } returns entities

        val result = repository.getAllSuppliers()

        assertEquals(2, result.size)
        assertEquals("Supplier A", result[0].name)
        assertEquals("Supplier B", result[1].name)
        coVerify { supplierDao.getAll() }
    }

    @Test
    fun `getSupplierById returns mapped supplier when found`() = runTest {
        val entity = SupplierEntity(5, "Supplier X", "Alice", "111222333", "x@example.com", "Address X")
        coEvery { supplierDao.getById(5) } returns entity

        val result = repository.getSupplierById(5)

        assertNotNull(result)
        assertEquals("Supplier X", result?.name)
        coVerify { supplierDao.getById(5) }
    }

    @Test
    fun `getSupplierById returns null when not found`() = runTest {
        coEvery { supplierDao.getById(999) } returns null

        val result = repository.getSupplierById(999)

        assertNull(result)
        coVerify { supplierDao.getById(999) }
    }

    @Test
    fun `insertSupplier delegates to DAO with correct entity`() = runTest {
        val supplier = Supplier(0, "New Supplier", "Bob", "555666777", "new@example.com", "New Address")
        coEvery { supplierDao.insert(any()) } returns 1L

        repository.insertSupplier(supplier)

        coVerify {
            supplierDao.insert(match {
                it.name == "New Supplier" && it.contactPerson == "Bob" && it.email == "new@example.com"
            })
        }
    }

    @Test
    fun `updateSupplier delegates to DAO with correct entity`() = runTest {
        val supplier = Supplier(10, "Updated Supplier", "Carol", "999888777", "upd@example.com", "Upd Address")

        repository.updateSupplier(supplier)

        coVerify {
            supplierDao.update(match { it.id == 10 && it.name == "Updated Supplier" })
        }
    }

    @Test
    fun `deleteSupplier delegates to DAO with correct entity`() = runTest {
        val supplier = Supplier(15, "Delete Supplier", "Dave", "333444555", "del@example.com", "Del Address")

        repository.deleteSupplier(supplier)

        coVerify {
            supplierDao.delete(match { it.id == 15 })
        }
    }

    @Test
    fun `searchSuppliers returns mapped list`() = runTest {
        val entities = listOf(
            SupplierEntity(20, "Search Supplier", "Eve", "123123123", "search@example.com", "Search Address")
        )
        coEvery { supplierDao.search("search") } returns entities

        val result = repository.searchSuppliers("search")

        assertEquals(1, result.size)
        assertEquals("Search Supplier", result[0].name)
        coVerify { supplierDao.search("search") }
    }
    //endregion

    //region transactions
    @Test
    fun `getAllTransactions returns mapped list`() = runTest {
        val entities = listOf(
            TransactionEntity(1, 1680000000000, "restock", 1, 10, "Initial stock"),
            TransactionEntity(2, 1680001000000, "sale", 1, 2, "Sold 2 items")
        )
        coEvery { transactionDao.getAll() } returns entities

        val result = repository.getAllTransactions()

        assertEquals(2, result.size)
        assertEquals("restock", result[0].type)
        assertEquals("sale", result[1].type)
        coVerify { transactionDao.getAll() }
    }

    @Test
    fun `getTransactionsByType returns filtered transactions`() = runTest {
        val entities = listOf(
            TransactionEntity(10, 1680000000000, "sale", 5, 1, "Sold one")
        )
        coEvery { transactionDao.getByType("sale") } returns entities

        val result = repository.getTransactionsByType("sale")

        assertEquals(1, result.size)
        assertEquals("sale", result[0].type)
        coVerify { transactionDao.getByType("sale") }
    }

    @Test
    fun `getTransactionsByProduct returns filtered transactions`() = runTest {
        val entities = listOf(
            TransactionEntity(15, 1680005000000, "restock", 7, 20, "Restocked 20")
        )
        coEvery { transactionDao.getByProduct(7) } returns entities

        val result = repository.getTransactionsByProduct(7)

        assertEquals(1, result.size)
        assertEquals(7, result[0].productId)
        coVerify { transactionDao.getByProduct(7) }
    }

    @Test
    fun `filterTransactionsByDateRange returns correct transactions`() = runTest {
        val from = 1680000000000L
        val to = 1680100000000L
        val entities = listOf(
            TransactionEntity(20, 1680005000000, "restock", 3, 10, null)
        )
        coEvery { transactionDao.filterByDateRange(from, to) } returns entities

        val result = repository.filterTransactionsByDateRange(from, to)

        assertEquals(1, result.size)
        assertEquals(20, result[0].id)
        coVerify { transactionDao.filterByDateRange(from, to) }
    }

    @Test
    fun `filterTransactionsByTypeAndProduct returns filtered transactions`() = runTest {
        val entities = listOf(
            TransactionEntity(25, 1680008000000, "sale", 9, 3, "Sold 3")
        )
        coEvery { transactionDao.filterByTypeAndProduct("sale", 9) } returns entities

        val result = repository.filterTransactionsByTypeAndProduct("sale", 9)

        assertEquals(1, result.size)
        assertEquals("sale", result[0].type)
        assertEquals(9, result[0].productId)
        coVerify { transactionDao.filterByTypeAndProduct("sale", 9) }
    }

    @Test
    fun `insertTransaction delegates to DAO with correct entity`() = runTest {
        val transaction = Transaction(0, 1680000000000, "restock", 4, 15, "New stock")
        coEvery { transactionDao.insert(any()) } returns 100L

        repository.insertTransaction(transaction)

        coVerify {
            transactionDao.insert(match { it.type == "restock" && it.productId == 4 && it.quantity == 15 })
        }
    }
    //endregion transactions
}
