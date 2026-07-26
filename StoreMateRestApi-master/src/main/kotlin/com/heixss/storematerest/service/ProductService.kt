package com.heixss.storematerest.service

import com.heixss.storematerest.exception.ForeignStoreException
import com.heixss.storematerest.exception.InvalidRequestException
import com.heixss.storematerest.exception.NotFoundException
import com.heixss.storematerest.model.Product
import com.heixss.storematerest.model.ProductDTO
import com.heixss.storematerest.model.Supplier
import com.heixss.storematerest.repository.ProductRepository
import com.heixss.storematerest.repository.SupplierRepository
import com.heixss.storematerest.repository.TransactionRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val repo: ProductRepository,
    private val userService: UserService,
    private val supplierRepo: SupplierRepository,
    private val transactionRepo: TransactionRepository
) {

    @Transactional(readOnly = true)
    fun getAll(username: String, page: Int, size: Int): Page<Product> {
        val user = userService.getByUsername(username)
        return repo.findAllByStoreId(
            user.store.id,
            PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"))
        )
    }

    @Transactional(readOnly = true)
    fun get(username: String, id: Long): Product =
        requireOwned(username, id)

    @Transactional
    fun create(username: String, req: ProductDTO): Product {
        val user = userService.getByUsername(username)
        val supplier = resolveSupplier(user.store.id, req.supplierId)

        return repo.save(
            Product(
                name = req.name.trim(),
                description = req.description,
                price = req.price,
                category = req.category,
                barcode = req.barcode,
                supplier = supplier,
                currentStockLevel = req.currentStockLevel,
                minimumStockLevel = req.minimumStockLevel,
                store = user.store
            )
        )
    }

    @Transactional
    fun update(username: String, id: Long, req: ProductDTO): Product {
        val existing = requireOwned(username, id)
        val supplier = resolveSupplier(existing.store.id, req.supplierId)

        return repo.save(
            existing.copy(
                name = req.name.trim(),
                description = req.description,
                price = req.price,
                category = req.category,
                barcode = req.barcode,
                supplier = supplier,
                currentStockLevel = req.currentStockLevel,
                minimumStockLevel = req.minimumStockLevel
            )
        )
    }

    @Transactional
    fun delete(username: String, id: Long) {
        val product = requireOwned(username, id)
        // Transactions reference the product with a non-null FK, so the history
        // goes with it. This mirrors the app's local Room CASCADE.
        transactionRepo.deleteAll(transactionRepo.findAllByProductId(product.id))
        repo.delete(product)
    }

    private fun requireOwned(username: String, id: Long): Product {
        val user = userService.getByUsername(username)
        val product = repo.findById(id).orElseThrow { NotFoundException("Product $id not found") }
        if (product.store.id != user.store.id) {
            throw ForeignStoreException("Product $id not found")
        }
        return product
    }

    private fun resolveSupplier(storeId: Long, supplierId: Long?): Supplier? {
        if (supplierId == null || supplierId <= 0) return null
        val supplier = supplierRepo.findById(supplierId)
            .orElseThrow { NotFoundException("Supplier $supplierId not found") }
        if (supplier.store.id != storeId) {
            throw InvalidRequestException("Supplier does not belong to your store")
        }
        return supplier
    }
}
