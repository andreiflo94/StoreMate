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

val sampleProducts = listOf(
    Product(
        id = 1,
        name = "Coca-Cola",
        description = "Bautura racoritoare",
        price = 5.0,
        category = "Bauturi",
        barcode = "1234567890",
        supplierId = 1,
        currentStockLevel = 2,
        minimumStockLevel = 5
    ),
    Product(
        id = 2,
        name = "Pepsi",
        description = "Bautura racoritoare",
        price = 4.5,
        category = "Bauturi",
        barcode = "0987654321",
        supplierId = 2,
        currentStockLevel = 1,
        minimumStockLevel = 4
    )
)

val sampleTransactions = listOf(
    TransactionWithProductName(
        transaction = Transaction(
            id = 1,
            date = System.currentTimeMillis(),
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
            date = System.currentTimeMillis(),
            type = "restock",
            productId = 2,
            quantity = 10,
            notes = "Revenit pe stoc"
        ),
        productName = "Pepsi"
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


