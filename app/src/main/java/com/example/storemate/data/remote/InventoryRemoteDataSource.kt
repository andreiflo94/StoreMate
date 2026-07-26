package com.example.storemate.data.remote

import com.example.storemate.data.mapper.toDomain
import com.example.storemate.data.mapper.toCreateDto
import com.example.storemate.data.mapper.toDto
import com.example.storemate.data.remote.dto.PageDto
import com.example.storemate.domain.model.Product
import com.example.storemate.domain.model.Supplier
import com.example.storemate.domain.model.Transaction

/**
 * Talks to the on-prem server and hands back domain models.
 *
 * List endpoints are paginated, but the app mirrors the whole store into its
 * local cache, so every `fetchAll*` walks the pages to completion. A single
 * shop's catalogue is small enough that this is a few requests at most.
 */
class InventoryRemoteDataSource(private val api: StoreMateApi) {

    suspend fun fetchAllProducts(): List<Product> =
        fetchAllPages { page -> api.getProducts(page, PAGE_SIZE) }.map { it.toDomain() }

    suspend fun fetchAllSuppliers(): List<Supplier> =
        fetchAllPages { page -> api.getSuppliers(page, PAGE_SIZE) }.map { it.toDomain() }

    suspend fun fetchAllTransactions(): List<Transaction> =
        fetchAllPages { page -> api.getTransactions(page, PAGE_SIZE) }.map { it.toDomain() }

    suspend fun fetchProduct(id: Int): Product = api.getProduct(id.toLong()).toDomain()

    suspend fun createProduct(product: Product): Product =
        api.createProduct(product.toDto()).toDomain()

    suspend fun updateProduct(product: Product): Product =
        api.updateProduct(product.id.toLong(), product.toDto()).toDomain()

    suspend fun deleteProduct(product: Product) =
        api.deleteProduct(product.id.toLong())

    suspend fun createSupplier(supplier: Supplier): Supplier =
        api.createSupplier(supplier.toDto()).toDomain()

    suspend fun updateSupplier(supplier: Supplier): Supplier =
        api.updateSupplier(supplier.id.toLong(), supplier.toDto()).toDomain()

    suspend fun deleteSupplier(supplier: Supplier) =
        api.deleteSupplier(supplier.id.toLong())

    suspend fun createTransaction(transaction: Transaction): Transaction =
        api.createTransaction(transaction.toCreateDto()).toDomain()

    private suspend fun <T> fetchAllPages(load: suspend (Int) -> PageDto<T>): List<T> {
        val all = mutableListOf<T>()
        var page = 0
        while (true) {
            val response = load(page)
            all += response.content
            if (response.last || response.content.isEmpty() || page >= MAX_PAGES) break
            page++
        }
        return all
    }

    private companion object {
        const val PAGE_SIZE = 200

        /** Guards against a malformed `last` flag turning sync into an infinite loop. */
        const val MAX_PAGES = 500
    }
}
