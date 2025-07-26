package com.example.storemate.data.mapper

import com.example.storemate.data.dbentities.ProductEntity
import com.example.storemate.data.dbentities.SupplierEntity
import com.example.storemate.data.dbentities.TransactionEntity
import com.example.storemate.data.dbentities.TransactionWithProductNameEntity
import com.example.storemate.domain.model.Product
import com.example.storemate.domain.model.Supplier
import com.example.storemate.domain.model.Transaction
import com.example.storemate.domain.model.TransactionWithProductName

//region Data Entity to Domain
fun ProductEntity.toDomain() = Product(
    id = id,
    name = name,
    description = description,
    price = price,
    category = category,
    barcode = barcode,
    supplierId = supplierId,
    currentStockLevel = currentStockLevel,
    minimumStockLevel = minimumStockLevel
)

fun SupplierEntity.toDomain() = Supplier(
    id = id,
    name = name,
    contactPerson = contactPerson,
    phone = phone,
    email = email,
    address = address
)

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    date = date,
    type = type,
    productId = productId,
    quantity = quantity,
    notes = notes
)

fun TransactionWithProductNameEntity.toDomain() = TransactionWithProductName(
    transaction = transactionEntity.toDomain(),
    productName = productName
)
//endregion

//region Domain to Data Entity

fun Product.toEntity() = ProductEntity(
    id = id,
    name = name,
    description = description,
    price = price,
    category = category,
    barcode = barcode,
    supplierId = supplierId,
    currentStockLevel = currentStockLevel,
    minimumStockLevel = minimumStockLevel
)

fun Supplier.toEntity() = SupplierEntity(
    id = id,
    name = name,
    contactPerson = contactPerson,
    phone = phone,
    email = email,
    address = address
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    date = date,
    type = type,
    productId = productId,
    quantity = quantity,
    notes = notes
)

fun TransactionWithProductName.toEntity() = TransactionWithProductNameEntity(
    transactionEntity = transaction.toEntity(),
    productName = productName
)
//endregion
