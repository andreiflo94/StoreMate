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

//region mocked data, modifying this could cause unit/integration tests to fail.
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
        description = "Refreshing soft drink",
        price = 4.5,
        category = "Beverages",
        barcode = "0987654321",
        supplierId = 2,
        currentStockLevel = 15,
        minimumStockLevel = 10
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
        minimumStockLevel = 15
    ),
    Product(
        id = 4,
        name = "Fairy Dishwashing Liquid",
        description = "Liquid detergent for dishes",
        price = 12.0,
        category = "Cleaning",
        barcode = "5566778899",
        supplierId = 4,
        currentStockLevel = 8,
        minimumStockLevel = 5
    ),
    Product(
        id = 5,
        name = "Dove Soap",
        description = "Moisturizing soap bar",
        price = 6.0,
        category = "Personal Care",
        barcode = "6677889900",
        supplierId = 5,
        currentStockLevel = 25,
        minimumStockLevel = 10
    ),
    Product(
        id = 6,
        name = "Lipton Green Tea",
        description = "Green tea bags",
        price = 10.0,
        category = "Groceries",
        barcode = "7788990011",
        supplierId = 3,
        currentStockLevel = 40,
        minimumStockLevel = 20
    ),
    Product(
        id = 7,
        name = "Pantene Shampoo",
        description = "Hair shampoo for shine",
        price = 15.0,
        category = "Personal Care",
        barcode = "8899001122",
        supplierId = 5,
        currentStockLevel = 10,
        minimumStockLevel = 7
    ),
    Product(
        id = 8,
        name = "Gillette Razor",
        description = "Men's razor blades",
        price = 30.0,
        category = "Personal Care",
        barcode = "9900112233",
        supplierId = 4,
        currentStockLevel = 12,
        minimumStockLevel = 6
    )
)

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
    ),
    Supplier(
        id = 4,
        name = "Procter & Gamble",
        contactPerson = "Elena Marin",
        phone = "0755 555 555",
        email = "elena.marin@pg.com",
        address = "Str. Libertatii 8, Timisoara"
    ),
    Supplier(
        id = 5,
        name = "Unilever Romania",
        contactPerson = "Mihai Stan",
        phone = "0766 888 999",
        email = "mihai.stan@unilever.com",
        address = "Str. Aviatorilor 22, Brasov"
    )
)

val sampleTransactions = listOf(
    TransactionWithProductName(
        transaction = Transaction(
            id = 1,
            date = System.currentTimeMillis() - 86400000, // 1 day ago
            type = "sale",
            productId = 1,
            quantity = 3,
            notes = null
        ),
        productName = "Coca-Cola"
    ),
    TransactionWithProductName(
        transaction = Transaction(
            id = 2,
            date = System.currentTimeMillis() - 43200000, // 12 hours ago
            type = "restock",
            productId = 2,
            quantity = 10,
            notes = "Restocked after shipment"
        ),
        productName = "Pepsi"
    ),
    TransactionWithProductName(
        transaction = Transaction(
            id = 3,
            date = System.currentTimeMillis() - 259200000, // 3 days ago
            type = "sale",
            productId = 3,
            quantity = 5,
            notes = "Black Friday sale"
        ),
        productName = "Nescafe Classic"
    ),
    TransactionWithProductName(
        transaction = Transaction(
            id = 4,
            date = System.currentTimeMillis() - 604800000, // 7 days ago
            type = "restock",
            productId = 5,
            quantity = 20,
            notes = null
        ),
        productName = "Dove Soap"
    ),
    TransactionWithProductName(
        transaction = Transaction(
            id = 5,
            date = System.currentTimeMillis() - 7200000, // 2 hours ago
            type = "sale",
            productId = 6,
            quantity = 7,
            notes = "Morning rush"
        ),
        productName = "Lipton Green Tea"
    )
)

val defaultSupplier = Supplier(
    id = 1,
    name = "Supplier A",
    contactPerson = "John Doe",
    phone = "123456789",
    email = "a@example.com",
    address = "Address 1"
)

val defaultProduct = Product(
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
//endregion
