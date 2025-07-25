package com.example.storemate.data

import com.example.storemate.data.dbentities.ProductEntity
import com.example.storemate.data.dbentities.SupplierEntity
import com.example.storemate.data.dbentities.TransactionEntity
import com.example.storemate.data.mapper.toDomain
import com.example.storemate.data.mapper.toEntity
import com.example.storemate.domain.model.Product
import com.example.storemate.domain.model.Supplier
import com.example.storemate.domain.model.Transaction
import junit.framework.TestCase.assertEquals
import org.junit.Test

class MapperTest {

    @Test
    fun `ProductEntity toDomain maps all fields`() {
        val entity = ProductEntity(
            id = 1,
            name = "Prod",
            description = "Desc",
            price = 10.5,
            category = "Cat",
            barcode = "B123",
            supplierId = 7,
            currentStockLevel = 20,
            minimumStockLevel = 5
        )

        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.name, domain.name)
        assertEquals(entity.description, domain.description)
        assertEquals(entity.price, domain.price)
        assertEquals(entity.category, domain.category)
        assertEquals(entity.barcode, domain.barcode)
        assertEquals(entity.supplierId, domain.supplierId)
        assertEquals(entity.currentStockLevel, domain.currentStockLevel)
        assertEquals(entity.minimumStockLevel, domain.minimumStockLevel)
    }

    @Test
    fun `Product toEntity maps all fields`() {
        val domain = Product(
            id = 2,
            name = "DomainProd",
            description = "DomainDesc",
            price = 15.5,
            category = "DomainCat",
            barcode = "D123",
            supplierId = 1,
            currentStockLevel = 30,
            minimumStockLevel = 10
        )

        val entity = domain.toEntity()

        assertEquals(domain.id, entity.id)
        assertEquals(domain.name, entity.name)
        assertEquals(domain.description, entity.description)
        assertEquals(domain.price, entity.price)
        assertEquals(domain.category, entity.category)
        assertEquals(domain.barcode, entity.barcode)
        assertEquals(domain.supplierId, entity.supplierId)
        assertEquals(domain.currentStockLevel, entity.currentStockLevel)
        assertEquals(domain.minimumStockLevel, entity.minimumStockLevel)
    }

    @Test
    fun `SupplierEntity toDomain maps all fields`() {
        val entity = SupplierEntity(
            id = 10,
            name = "SupplierX",
            contactPerson = "PersonX",
            phone = "123456",
            email = "x@example.com",
            address = "AddrX"
        )

        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.name, domain.name)
        assertEquals(entity.contactPerson, domain.contactPerson)
        assertEquals(entity.phone, domain.phone)
        assertEquals(entity.email, domain.email)
        assertEquals(entity.address, domain.address)
    }

    @Test
    fun `Supplier toEntity maps all fields`() {
        val domain = Supplier(
            id = 20,
            name = "SupplierY",
            contactPerson = "PersonY",
            phone = "654321",
            email = "y@example.com",
            address = "AddrY"
        )

        val entity = domain.toEntity()

        assertEquals(domain.id, entity.id)
        assertEquals(domain.name, entity.name)
        assertEquals(domain.contactPerson, entity.contactPerson)
        assertEquals(domain.phone, entity.phone)
        assertEquals(domain.email, entity.email)
        assertEquals(domain.address, entity.address)
    }

    @Test
    fun `TransactionEntity toDomain maps all fields`() {
        val entity = TransactionEntity(
            id = 100,
            date = 1680000000000,
            type = "restock",
            productId = 5,
            quantity = 10,
            notes = "Restocked 10"
        )

        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.date, domain.date)
        assertEquals(entity.type, domain.type)
        assertEquals(entity.productId, domain.productId)
        assertEquals(entity.quantity, domain.quantity)
        assertEquals(entity.notes, domain.notes)
    }

    @Test
    fun `Transaction toEntity maps all fields`() {
        val domain = Transaction(
            id = 200,
            date = 1680100000000,
            type = "sale",
            productId = 8,
            quantity = 3,
            notes = null
        )

        val entity = domain.toEntity()

        assertEquals(domain.id, entity.id)
        assertEquals(domain.date, entity.date)
        assertEquals(domain.type, entity.type)
        assertEquals(domain.productId, entity.productId)
        assertEquals(domain.quantity, entity.quantity)
        assertEquals(domain.notes, entity.notes)
    }
}
