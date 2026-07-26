package com.heixss.storematerest.model

import jakarta.persistence.*
import java.time.LocalDateTime

enum class TransactionType {
    RESTOCK, SALE
}

@Entity
@Table(name = "transactions")
data class Transaction(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val date: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    val type: TransactionType,

    @ManyToOne
    @JoinColumn(name = "product_id")
    val product: Product,

    val quantity: Int,

    val notes: String? = null,

    @ManyToOne
    @JoinColumn(name = "store_id")
    val store: Store
)
