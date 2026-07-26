package com.heixss.storematerest.service

import com.heixss.storematerest.exception.ForeignStoreException
import com.heixss.storematerest.exception.NotFoundException
import com.heixss.storematerest.model.Supplier
import com.heixss.storematerest.model.SupplierDTO
import com.heixss.storematerest.repository.ProductRepository
import com.heixss.storematerest.repository.SupplierRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SupplierService(
    private val repo: SupplierRepository,
    private val userService: UserService,
    private val productRepo: ProductRepository
) {

    @Transactional(readOnly = true)
    fun getAll(username: String, page: Int, size: Int): Page<Supplier> {
        val user = userService.getByUsername(username)
        return repo.findAllByStoreId(
            user.store.id,
            PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"))
        )
    }

    @Transactional(readOnly = true)
    fun get(username: String, id: Long): Supplier = requireOwned(username, id)

    @Transactional
    fun create(username: String, req: SupplierDTO): Supplier {
        val user = userService.getByUsername(username)
        return repo.save(
            Supplier(
                name = req.name.trim(),
                contactPerson = req.contactPerson,
                phone = req.phone,
                email = req.email,
                address = req.address,
                store = user.store
            )
        )
    }

    @Transactional
    fun update(username: String, id: Long, req: SupplierDTO): Supplier {
        val existing = requireOwned(username, id)
        return repo.save(
            existing.copy(
                name = req.name.trim(),
                contactPerson = req.contactPerson,
                phone = req.phone,
                email = req.email,
                address = req.address
            )
        )
    }

    @Transactional
    fun delete(username: String, id: Long) {
        val supplier = requireOwned(username, id)
        // Detach products first: they outlive their supplier, matching the
        // app's local ForeignKey.SET_NULL behaviour.
        val orphaned = productRepo.findAllBySupplierId(supplier.id)
            .map { it.copy(supplier = null) }
        productRepo.saveAll(orphaned)
        repo.delete(supplier)
    }

    private fun requireOwned(username: String, id: Long): Supplier {
        val user = userService.getByUsername(username)
        val supplier = repo.findById(id).orElseThrow { NotFoundException("Supplier $id not found") }
        if (supplier.store.id != user.store.id) {
            throw ForeignStoreException("Supplier $id not found")
        }
        return supplier
    }
}
