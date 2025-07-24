package com.example.storemate.repository.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.storemate.data.StoreMateDb
import com.example.storemate.data.repositories.InventoryRepositoryImpl
import com.example.storemate.domain.model.Product
import com.example.storemate.domain.model.Supplier
import com.example.storemate.domain.model.Transaction
import junit.framework.TestCase.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StoreMateDbTest {

    private lateinit var db: StoreMateDb
    private lateinit var repository: InventoryRepositoryImpl

    private val defaultSupplier = Supplier(
        id = 1,
        name = "Supplier A",
        contactPerson = "John Doe",
        phone = "123456789",
        email = "a@example.com",
        address = "Address 1"
    )

    private val defaultProduct = Product(
        id = 1,
        name = "Product A",
        description = "Description",
        price = 10.0,
        category = "Category",
        barcode = "123456",
        supplierId = 1,
        currentStockLevel = 100,
        minimumStockLevel = 10
    )

    @Before
    fun setUp() = runTest {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StoreMateDb::class.java
        ).build()
        repository = InventoryRepositoryImpl(db)
        repository.insertSupplier(defaultSupplier)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun insertProduct(product: Product = defaultProduct) {
        repository.insertProduct(product)
    }

    private fun createTransaction(
        date: Long = System.currentTimeMillis(),
        type: String,
        productId: Int = 1,
        quantity: Int,
        notes: String? = null
    ) = Transaction(date = date, type = type, productId = productId, quantity = quantity, notes = notes)

    //region products
    @Test
    fun `insertProduct and getProductById return correct product`() = runTest {
        insertProduct()

        val result = repository.getProductById(defaultProduct.id)

        assertNotNull(result)
        assertEquals(defaultProduct.name, result?.name)
        assertEquals(defaultProduct.barcode, result?.barcode)
    }

    @Test
    fun `updateProduct updates existing product`() = runTest {
        insertProduct()

        val updated = defaultProduct.copy(name = "Brown Sugar", price = 5.5)
        repository.updateProduct(updated)

        repository.getProductById(defaultProduct.id)?.let{ result ->
            assertEquals("Brown Sugar", result.name)
            assertEquals(5.5, result.price, 0.01)
        }
    }

    @Test
    fun `deleteProduct removes the product`() = runTest {
        insertProduct()

        repository.deleteProduct(defaultProduct)

        val result = repository.getProductById(defaultProduct.id)
        assertNull(result)
    }

    @Test
    fun `getAllProducts returns all inserted products`() = runTest {
        val products = listOf(
            Product(1, "Apple", "", 1.0, "Fruits", "111", 1, 5, 2),
            Product(2, "Banana", "", 1.2, "Fruits", "222", 1, 10, 3)
        )
        products.forEach { repository.insertProduct(it) }

        val result = repository.getAllProducts()

        assertEquals(products.size, result.size)
        products.forEach { p -> assertTrue(result.any { it.name == p.name }) }
    }

    @Test
    fun `getLowStockProducts returns products below threshold`() = runTest {
        val products = listOf(
            Product(1, "Cola", "", 3.0, "Drinks", "333", 1, 2, 5), // low stock
            Product(2, "Fanta", "", 3.0, "Drinks", "444", 1, 10, 5) // ok
        )
        products.forEach { repository.insertProduct(it) }

        val result = repository.getLowStockProducts()

        assertEquals(1, result.size)
        assertEquals("Cola", result[0].name)
    }

    @Test
    fun `searchProducts returns matching results`() = runTest {
        val products = listOf(
            Product(1, "Orange Juice", "", 5.0, "Drinks", "123", 1, 5, 2),
            Product(2, "Apple Juice", "", 4.0, "Drinks", "124", 1, 5, 2)
        )
        products.forEach { repository.insertProduct(it) }

        val result = repository.searchProducts("apple")
        assertEquals(1, result.size)
        assertEquals("Apple Juice", result[0].name)
    }

    @Test
    fun `searchProducts is case insensitive`() = runTest {
        insertProduct(Product(1, "ChocoBar", "", 2.0, "Sweets", "999", 1, 10, 3))

        val result = repository.searchProducts("choco")
        assertEquals(1, result.size)
        assertEquals("ChocoBar", result[0].name)
    }

    @Test
    fun `searchProducts returns empty list if no match`() = runTest {
        insertProduct(Product(1, "Milk", "", 2.0, "Dairy", "888", 1, 10, 3))

        val result = repository.searchProducts("nonexistent")
        assertTrue(result.isEmpty())
    }
    //endregion

    //region suppliers
    @Test
    fun `insertSupplier should add a new supplier`() = runTest {
        val newSupplier = Supplier(2, "Supplier B", "Jane Smith", "987654321", "b@example.com", "Address 2")
        repository.insertSupplier(newSupplier)

        val result = repository.getSupplierById(newSupplier.id)
        assertNotNull(result)
        assertEquals(newSupplier.name, result?.name)
    }

    @Test
    fun `getAllSuppliers should return all inserted suppliers`() = runTest {
        val suppliers = listOf(
            defaultSupplier,
            Supplier(2, "Supplier B", "Jane Smith", "987654321", "b@example.com", "Address 2")
        )
        suppliers.forEach { repository.insertSupplier(it) }

        val result = repository.getAllSuppliers()

        assertEquals(suppliers.size, result.size)
        suppliers.forEach { s -> assertTrue(result.any { it.name == s.name }) }
    }

    @Test
    fun `getSupplierById should return null for non-existent supplier`() = runTest {
        val result = repository.getSupplierById(999)
        assertNull(result)
    }

    @Test
    fun `deleteSupplier should remove supplier from db`() = runTest {
        repository.deleteSupplier(defaultSupplier)

        val result = repository.getSupplierById(defaultSupplier.id)
        assertNull(result)
    }

    @Test
    fun `updateSupplier should modify existing supplier`() = runTest {
        val updatedSupplier = defaultSupplier.copy(name = "Updated Supplier", contactPerson = "Alex New")
        repository.updateSupplier(updatedSupplier)

        val result = repository.getSupplierById(defaultSupplier.id)
        assertNotNull(result)
        assertEquals("Updated Supplier", result?.name)
        assertEquals("Alex New", result?.contactPerson)
    }
    //endregion

    //region transactions
    @Test
    fun `insertTransaction should add a new transaction`() = runTest {
        insertProduct()

        val transaction = createTransaction(
            date = 1721808000000,
            type = "sale",
            productId = defaultProduct.id,
            quantity = 5,
            notes = "Sold 5 units"
        )
        repository.insertTransaction(transaction)

        val all = repository.getAllTransactions()
        val result = all.firstOrNull()
        assertNotNull(result)
        assertEquals("sale", result?.type)
        assertEquals(5, result?.quantity)
    }

    @Test
    fun `getAllTransactions should return all inserted transactions`() = runTest {
        insertProduct()

        val transactions = listOf(
            createTransaction(date = 1721779200000, type = "PURCHASE", quantity = 20, notes = "Bought stock"),
            createTransaction(date = 1721865600000, type = "sale", quantity = 5, notes = "Sold items")
        )
        transactions.forEach { repository.insertTransaction(it) }

        val result = repository.getAllTransactions()
        assertEquals(2, result.size)
        assertTrue(result.any { it.type == "PURCHASE" })
        assertTrue(result.any { it.type == "sale" })
    }

    @Test
    fun `getTransactionsByProductId should return only transactions for that product`() = runTest {
        val product2 = defaultProduct.copy(id = 2, name = "Product B", price = 20.0, currentStockLevel = 50)
        insertProduct(defaultProduct)
        insertProduct(product2)

        val tx1 = createTransaction(type = "restock", productId = 1, quantity = 10)
        val tx2 = createTransaction(type = "sale", productId = 2, quantity = 5)
        repository.insertTransaction(tx1)
        repository.insertTransaction(tx2)

        val result = repository.getTransactionsByProduct(1)
        assertEquals(1, result.size)
        assertEquals(1, result[0].productId)
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun `insertTransaction with invalid productId should throw SQLiteConstraintException`() = runTest {
        val invalidTransaction = createTransaction(type = "sale", productId = 999, quantity = 1)
        repository.insertTransaction(invalidTransaction)
    }
    //endregion
}
