package com.heixss.storematerest.service

import com.heixss.storematerest.exception.ForeignStoreException
import com.heixss.storematerest.exception.InvalidRequestException
import com.heixss.storematerest.exception.NotFoundException
import com.heixss.storematerest.model.Product
import com.heixss.storematerest.model.Transaction
import com.heixss.storematerest.model.TransactionDTO
import com.heixss.storematerest.model.TransactionType
import com.heixss.storematerest.repository.ProductRepository
import com.heixss.storematerest.repository.TransactionRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

@Service
class TransactionService(
    private val repo: TransactionRepository,
    private val userService: UserService,
    private val productRepo: ProductRepository
) {

    @Transactional(readOnly = true)
    fun getAll(username: String, page: Int, size: Int): Page<Transaction> {
        val user = userService.getByUsername(username)
        return repo.findAllByStoreId(
            user.store.id,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"))
        )
    }

    @Transactional(readOnly = true)
    fun get(username: String, id: Long): Transaction {
        val user = userService.getByUsername(username)
        val transaction = repo.findById(id)
            .orElseThrow { NotFoundException("Transaction $id not found") }
        if (transaction.store.id != user.store.id) {
            throw ForeignStoreException("Transaction $id not found")
        }
        return transaction
    }

    /**
     * Records a transaction **and moves stock**. A restock increases the
     * product's stock level, a sale decreases it. Previously transactions were
     * recorded without ever touching `currentStockLevel`, so the low-stock
     * dashboard could never change.
     */
    @Transactional
    fun create(username: String, req: TransactionDTO): Transaction {
        val user = userService.getByUsername(username)
        val product = productRepo.findById(req.productId)
            .orElseThrow { NotFoundException("Product ${req.productId} not found") }

        if (product.store.id != user.store.id) {
            throw InvalidRequestException("Product does not belong to your store")
        }

        val type = parseType(req.type)
        val date = parseDate(req.date)

        productRepo.save(product.withStockAdjustedFor(type, req.quantity))

        return repo.save(
            Transaction(
                date = date,
                type = type,
                product = product,
                quantity = req.quantity,
                notes = req.notes,
                store = user.store
            )
        )
    }

    private fun Product.withStockAdjustedFor(type: TransactionType, quantity: Int): Product {
        val delta = when (type) {
            TransactionType.RESTOCK -> quantity
            TransactionType.SALE -> -quantity
        }
        val newLevel = currentStockLevel + delta
        if (newLevel < 0) {
            throw InvalidRequestException(
                "Cannot sell $quantity of '$name': only $currentStockLevel in stock"
            )
        }
        return copy(currentStockLevel = newLevel)
    }

    private fun parseType(raw: String): TransactionType =
        runCatching { TransactionType.valueOf(raw.trim().uppercase()) }
            .getOrElse {
                throw InvalidRequestException(
                    "Unknown transaction type '$raw' (expected RESTOCK or SALE)"
                )
            }

    private fun parseDate(raw: String?): LocalDateTime {
        if (raw.isNullOrBlank()) return LocalDateTime.now()
        return try {
            LocalDateTime.parse(raw)
        } catch (ex: DateTimeParseException) {
            throw InvalidRequestException("Invalid date '$raw' (expected ISO-8601, e.g. 2025-09-27T14:00:00)")
        }
    }
}
