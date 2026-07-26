package com.heixss.storematerest.model

import jakarta.persistence.*

@Entity
@Table(name = "suppliers")
data class Supplier(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    val contactPerson: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,

    @ManyToOne
    @JoinColumn(name = "store_id")
    val store: Store
)
