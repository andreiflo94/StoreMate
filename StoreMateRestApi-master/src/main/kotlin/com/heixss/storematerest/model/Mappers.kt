package com.heixss.storematerest.model

import org.springframework.data.domain.Page

fun Supplier.toResponse() = SupplierResponse(
    id = id,
    name = name,
    contactPerson = contactPerson.orEmpty(),
    phone = phone.orEmpty(),
    email = email.orEmpty(),
    address = address.orEmpty()
)

fun Product.toResponse() = ProductResponse(
    id = id,
    name = name,
    description = description.orEmpty(),
    price = price,
    category = category.orEmpty(),
    barcode = barcode.orEmpty(),
    supplierId = supplier?.id,
    supplierName = supplier?.name,
    currentStockLevel = currentStockLevel,
    minimumStockLevel = minimumStockLevel
)

fun Transaction.toResponse() = TransactionResponse(
    id = id,
    date = date,
    type = type.name,
    productId = product.id,
    productName = product.name,
    quantity = quantity,
    notes = notes
)

fun <E, R> Page<E>.toResponse(mapper: (E) -> R) = PageResponse(
    content = content.map(mapper),
    page = number,
    size = size,
    totalElements = totalElements,
    totalPages = totalPages,
    last = isLast
)
