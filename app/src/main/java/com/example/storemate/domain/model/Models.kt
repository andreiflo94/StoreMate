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

enum class TransactionType {
    restock, sale
}

val sampleSuppliers = listOf(
    Supplier(
        id = 1,
        name = "Coca-Cola HBC Romania",
        contactPerson = "Andrei Popescu",
        phone = "0722 123 456",
        email = "andrei.popescu@coca-cola.ro",
        address = "Str. Fabricii 10, Bucuresti"
    ),
    Supplier(
        id = 2,
        name = "PepsiCo Romania",
        contactPerson = "Ioana Ionescu",
        phone = "0733 987 654",
        email = "ioana.ionescu@pepsi.ro",
        address = "Bd. Industriilor 45, Bucuresti"
    ),
    Supplier(
        id = 3,
        name = "Nestle Distribution",
        contactPerson = "Vlad Georgescu",
        phone = "0744 111 222",
        email = "vlad.georgescu@nestle.com",
        address = "Calea Victoriei 100, Cluj-Napoca"
    )
)

val sampleProducts = listOf(
    Product(
        id = 1,
        name = "Coca-Cola",
        description = "Refreshing soft drink",
        price = 5.0,
        category = "Beverages",
        barcode = "1234567890",
        supplierId = 1,
        currentStockLevel = 20,
        minimumStockLevel = 10
    ),
    Product(
        id = 2,
        name = "Pepsi",
        description = "Refreshing cola drink",
        price = 4.5,
        category = "Beverages",
        barcode = "0987654321",
        supplierId = 2,
        currentStockLevel = 15,
        minimumStockLevel = 5
    ),
    Product(
        id = 3,
        name = "Nescafe Classic",
        description = "Instant coffee",
        price = 25.0,
        category = "Groceries",
        barcode = "1122334455",
        supplierId = 3,
        currentStockLevel = 30,
        minimumStockLevel = 10
    )
)

val sampleTransactions = listOf(
    TransactionWithProductName(
        transaction = Transaction(
            id = 1,
            date = System.currentTimeMillis() - 2 * 86400000, // 2 zile în urmă
            type = "sale",
            productId = 1,
            quantity = 2,
            notes = "Quick sale"
        ),
        productName = "Coca-Cola"
    ),
    TransactionWithProductName(
        transaction = Transaction(
            id = 2,
            date = System.currentTimeMillis() - 3 * 86400000,
            type = "restock",
            productId = 2,
            quantity = 15,
            notes = "Weekly restock"
        ),
        productName = "Pepsi"
    ),
    TransactionWithProductName(
        transaction = Transaction(
            id = 3,
            date = System.currentTimeMillis() - 6 * 86400000,
            type = "sale",
            productId = 3,
            quantity = 1,
            notes = "Small coffee order"
        ),
        productName = "Nescafe Classic"
    )
)

val defaultSupplier = sampleSuppliers.first()

val defaultProduct = Product(
    id = 99,
    name = "Mock Product",
    description = "This is a test product",
    price = 9.99,
    category = "Test",
    barcode = "0000009999",
    supplierId = defaultSupplier.id,
    currentStockLevel = 100,
    minimumStockLevel = 5
)
