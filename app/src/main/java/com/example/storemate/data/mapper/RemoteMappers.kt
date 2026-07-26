package com.example.storemate.data.mapper

import com.example.storemate.data.remote.dto.CreateTransactionDto
import com.example.storemate.data.remote.dto.ProductDto
import com.example.storemate.data.remote.dto.SupplierDto
import com.example.storemate.data.remote.dto.TransactionDto
import com.example.storemate.domain.model.Product
import com.example.storemate.domain.model.Supplier
import com.example.storemate.domain.model.Transaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException

/**
 * Maps between the REST wire format and the app's domain models.
 *
 * Two mismatches are bridged here:
 *  - the server keys entities with `Long`, the app with `Int`;
 *  - the server stores timestamps as ISO-8601 local date-times and spells
 *    transaction types in upper case, while the app uses epoch millis and the
 *    lower-case names of [com.example.storemate.domain.model.TransactionType].
 */

//region Remote to domain
fun SupplierDto.toDomain() = Supplier(
    id = id.toInt(),
    name = name,
    contactPerson = contactPerson,
    phone = phone,
    email = email,
    address = address
)

fun ProductDto.toDomain() = Product(
    id = id.toInt(),
    name = name,
    description = description,
    price = price,
    category = category,
    barcode = barcode,
    // The app models "no supplier" as id 0, matching how an unsaved product starts out.
    supplierId = supplierId?.toInt() ?: 0,
    currentStockLevel = currentStockLevel,
    minimumStockLevel = minimumStockLevel
)

fun TransactionDto.toDomain() = Transaction(
    id = id.toInt(),
    date = date.isoToEpochMillis(),
    type = type.lowercase(),
    productId = productId.toInt(),
    quantity = quantity,
    notes = notes
)
//endregion

//region Domain to remote
fun Supplier.toDto() = SupplierDto(
    id = id.toLong(),
    name = name,
    contactPerson = contactPerson,
    phone = phone,
    email = email,
    address = address
)

fun Product.toDto() = ProductDto(
    id = id.toLong(),
    name = name,
    description = description,
    price = price,
    category = category,
    barcode = barcode,
    supplierId = supplierId.takeIf { it > 0 }?.toLong(),
    currentStockLevel = currentStockLevel,
    minimumStockLevel = minimumStockLevel
)

fun Transaction.toCreateDto() = CreateTransactionDto(
    date = date.epochMillisToIso(),
    type = type.uppercase(),
    productId = productId.toLong(),
    quantity = quantity,
    notes = notes
)
//endregion

private fun String.isoToEpochMillis(): Long = try {
    LocalDateTime.parse(this).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
} catch (ex: DateTimeParseException) {
    // A timestamp we cannot read should not sink an entire sync; fall back to
    // "now" so the record still shows up in the history.
    System.currentTimeMillis()
}

private fun Long.epochMillisToIso(): String =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault()).toString()
