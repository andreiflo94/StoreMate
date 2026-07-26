package com.heixss.storematerest.controller

import com.heixss.storematerest.model.PageResponse
import com.heixss.storematerest.model.ProductDTO
import com.heixss.storematerest.model.ProductResponse
import com.heixss.storematerest.model.toResponse
import com.heixss.storematerest.service.ProductService
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
@RequestMapping("/api/products")
class ProductController(private val productService: ProductService) {

    @GetMapping
    fun getAll(
        @AuthenticationPrincipal user: UserDetails,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): PageResponse<ProductResponse> =
        productService.getAll(user.username, page, size).toResponse { it.toResponse() }

    @GetMapping("/{id}")
    fun get(
        @AuthenticationPrincipal user: UserDetails,
        @PathVariable id: Long
    ): ProductResponse = productService.get(user.username, id).toResponse()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @AuthenticationPrincipal user: UserDetails,
        @Valid @RequestBody product: ProductDTO
    ): ProductResponse = productService.create(user.username, product).toResponse()

    @PutMapping("/{id}")
    fun update(
        @AuthenticationPrincipal user: UserDetails,
        @PathVariable id: Long,
        @Valid @RequestBody product: ProductDTO
    ): ProductResponse = productService.update(user.username, id, product).toResponse()

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @AuthenticationPrincipal user: UserDetails,
        @PathVariable id: Long
    ) = productService.delete(user.username, id)
}
