package com.heixss.storematerest.service

import com.heixss.storematerest.exception.BadCredentialsError
import com.heixss.storematerest.exception.ConflictException
import com.heixss.storematerest.exception.NotFoundException
import com.heixss.storematerest.model.Store
import com.heixss.storematerest.model.User
import com.heixss.storematerest.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun register(username: String, rawPassword: String): User {
        val normalized = username.trim()
        if (normalized.isBlank()) throw IllegalArgumentException("Username is required")
        if (rawPassword.length < MIN_PASSWORD_LENGTH) {
            throw IllegalArgumentException("Password must be at least $MIN_PASSWORD_LENGTH characters")
        }
        if (userRepository.findByUsername(normalized) != null) {
            throw ConflictException("User '$normalized' already exists")
        }

        val store = Store(name = "$normalized's Store")
        return userRepository.save(
            User(
                username = normalized,
                password = passwordEncoder.encode(rawPassword),
                store = store
            )
        )
    }

    /** Returns the authenticated user, or throws so the caller renders a 401. */
    fun authenticate(username: String, rawPassword: String): User {
        val user = userRepository.findByUsername(username.trim()) ?: throw BadCredentialsError()
        if (!passwordEncoder.matches(rawPassword, user.password)) throw BadCredentialsError()
        return user
    }

    /**
     * Resolves the caller from the authenticated principal. The JWT filter has
     * already verified the signature by the time this runs.
     */
    fun getByUsername(username: String): User =
        userRepository.findByUsername(username) ?: throw NotFoundException("User not found")

    private companion object {
        const val MIN_PASSWORD_LENGTH = 4
    }
}

@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found")
        return org.springframework.security.core.userdetails.User(
            user.username,
            user.password,
            emptyList() // no roles yet
        )
    }
}
