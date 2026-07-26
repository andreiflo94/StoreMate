package com.heixss.storematerest.model

import jakarta.persistence.*

@Entity
@Table(name = "products")
data class Product(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    val description: String? = null,
    val price: Double = 0.0,
    val category: String? = null,
    val barcode: String? = null,

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    val supplier: Supplier? = null,

    val currentStockLevel: Int = 0,
    val minimumStockLevel: Int = 0,
    @ManyToOne
    @JoinColumn(name = "store_id")
    val store: Store
)
