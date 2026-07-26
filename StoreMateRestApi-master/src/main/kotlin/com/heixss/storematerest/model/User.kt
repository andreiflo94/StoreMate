package com.heixss.storematerest.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val username: String,
    val password: String,

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "store_id")
    val store: Store
)
