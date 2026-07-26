package com.example.storemate.data.mapper

import com.example.storemate.data.remote.dto.ProductDto
import com.example.storemate.data.remote.dto.TransactionDto
import com.example.storemate.domain.model.Transaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Guards the two mismatches between the REST contract and the app's models:
 * Long/Int ids, and ISO date-times plus upper-case enum names.
 */
class RemoteMappersTest {

    @Test
    fun `product without a supplier maps to id zero`() {
        val domain = ProductDto(id = 7, name = "Milk", supplierId = null).toDomain()

        assertEquals(0, domain.supplierId)
    }

    @Test
    fun `product with supplier id zero sends null to the server`() {
        val dto = ProductDto(id = 7, name = "Milk", supplierId = null).toDomain().toDto()

        assertNull(dto.supplierId)
    }

    @Test
    fun `server timestamp with microseconds parses to epoch millis`() {
        val dto = TransactionDto(
            id = 3,
            date = "2026-07-26T11:38:46.699885",
            type = "SALE",
            productId = 1,
            quantity = 4
        )

        val expected = LocalDateTime.of(2026, 7, 26, 11, 38, 46, 699_885_000)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        assertEquals(expected, dto.toDomain().date)
    }

    @Test
    fun `transaction type is lowercased for the app and uppercased for the server`() {
        val domain = TransactionDto(
            id = 1,
            date = "2026-07-26T10:00:00",
            type = "RESTOCK",
            productId = 1,
            quantity = 2
        ).toDomain()

        assertEquals("restock", domain.type)
        assertEquals("RESTOCK", domain.toCreateDto().type)
    }

    @Test
    fun `an unparseable date does not throw and sinks the whole sync`() {
        val before = System.currentTimeMillis()

        val domain = TransactionDto(
            id = 1,
            date = "not-a-date",
            type = "SALE",
            productId = 1,
            quantity = 1
        ).toDomain()

        // Falls back to "now" so the record still appears in the history.
        assert(domain.date >= before)
    }

    @Test
    fun `transaction date survives a round trip through the wire format`() {
        val original = Transaction(
            id = 1,
            // Truncated to whole seconds: the wire format has no sub-second field
            // when the value happens to land on a second boundary.
            date = LocalDateTime.of(2026, 3, 1, 9, 30, 15)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            type = "sale",
            productId = 4,
            quantity = 2,
            notes = "n"
        )

        val roundTripped = TransactionDto(
            id = original.id.toLong(),
            date = original.toCreateDto().date,
            type = original.toCreateDto().type,
            productId = original.productId.toLong(),
            quantity = original.quantity,
            notes = original.notes
        ).toDomain()

        assertEquals(original.date, roundTripped.date)
        assertEquals(original.type, roundTripped.type)
    }
}
