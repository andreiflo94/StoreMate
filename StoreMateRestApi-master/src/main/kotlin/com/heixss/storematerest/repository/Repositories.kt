package com.heixss.storematerest.repository

import com.heixss.storematerest.model.Product
import com.heixss.storematerest.model.Supplier
import com.heixss.storematerest.model.Transaction
import com.heixss.storematerest.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SupplierRepository : JpaRepository<Supplier, Long> {
    fun findAllByStoreId(storeId: Long, pageable: Pageable): Page<Supplier>
}

interface ProductRepository : JpaRepository<Product, Long> {
    fun findAllByStoreId(storeId: Long, pageable: Pageable): Page<Product>

    /** Used when a supplier is deleted, to detach its products. */
    fun findAllBySupplierId(supplierId: Long): List<Product>
}

interface TransactionRepository : JpaRepository<Transaction, Long> {
    fun findAllByStoreId(storeId: Long, pageable: Pageable): Page<Transaction>

    /** Used when a product is deleted, to remove its transaction history. */
    fun findAllByProductId(productId: Long): List<Transaction>
}

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
}
