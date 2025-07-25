package com.example.storemate.common

fun String.isValidPhoneNumber(): Boolean {
    val phoneRegex = Regex("^\\+?[0-9]{10,15}\$")
    return this.matches(phoneRegex)
}

fun String.isValidEmail(): Boolean {
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$")
    return this.matches(emailRegex)
}