package com.heixss.storematerest.controller

import com.heixss.storematerest.model.PageResponse
import com.heixss.storematerest.model.TransactionDTO
import com.heixss.storematerest.model.TransactionResponse
import com.heixss.storematerest.model.toResponse
import com.heixss.storematerest.service.TransactionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/transactions")
class TransactionController(private val transactionService: TransactionService) {

    @GetMapping
    fun getAll(
        @AuthenticationPrincipal user: UserDetails,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): PageResponse<TransactionResponse> =
        transactionService.getAll(user.username, page, size).toResponse { it.toResponse() }

    @GetMapping("/{id}")
    fun get(
        @AuthenticationPrincipal user: UserDetails,
        @PathVariable id: Long
    ): TransactionResponse = transactionService.get(user.username, id).toResponse()

    /** Recording a transaction also moves the product's stock level. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @AuthenticationPrincipal user: UserDetails,
        @Valid @RequestBody transaction: TransactionDTO
    ): TransactionResponse = transactionService.create(user.username, transaction).toResponse()
}
