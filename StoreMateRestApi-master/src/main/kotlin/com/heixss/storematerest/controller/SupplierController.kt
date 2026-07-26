package com.heixss.storematerest.controller

import com.heixss.storematerest.model.PageResponse
import com.heixss.storematerest.model.SupplierDTO
import com.heixss.storematerest.model.SupplierResponse
import com.heixss.storematerest.model.toResponse
import com.heixss.storematerest.service.SupplierService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/suppliers")
class SupplierController(private val supplierService: SupplierService) {

    @GetMapping
    fun getAll(
        @AuthenticationPrincipal user: UserDetails,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): PageResponse<SupplierResponse> =
        supplierService.getAll(user.username, page, size).toResponse { it.toResponse() }

    @GetMapping("/{id}")
    fun get(
        @AuthenticationPrincipal user: UserDetails,
        @PathVariable id: Long
    ): SupplierResponse = supplierService.get(user.username, id).toResponse()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @AuthenticationPrincipal user: UserDetails,
        @Valid @RequestBody supplier: SupplierDTO
    ): SupplierResponse = supplierService.create(user.username, supplier).toResponse()

    @PutMapping("/{id}")
    fun update(
        @AuthenticationPrincipal user: UserDetails,
        @PathVariable id: Long,
        @Valid @RequestBody supplier: SupplierDTO
    ): SupplierResponse = supplierService.update(user.username, id, supplier).toResponse()

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @AuthenticationPrincipal user: UserDetails,
        @PathVariable id: Long
    ) = supplierService.delete(user.username, id)
}
