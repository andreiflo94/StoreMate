package com.heixss.storematerest.model

import jakarta.persistence.*

@Entity
data class Store(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String
)
