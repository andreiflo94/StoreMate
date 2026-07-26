package com.heixss.storematerest.security

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtUtil(
    @Value("\${storemate.jwt.secret}") private val secret: String,
    @Value("\${storemate.jwt.expiration-minutes}") private val expirationMinutes: Long
) {
    private val log = LoggerFactory.getLogger(JwtUtil::class.java)

    private val key = Keys.hmacShaKeyFor(secret.toByteArray())
    private val expirationMs = expirationMinutes * 60 * 1000

    init {
        require(secret.toByteArray().size >= MIN_SECRET_BYTES) {
            "storemate.jwt.secret must be at least $MIN_SECRET_BYTES bytes for HS256"
        }
    }

    @PostConstruct
    fun warnOnDefaultSecret() {
        if (secret == PLACEHOLDER_SECRET) {
            log.warn(
                "\n================================================================\n" +
                    " StoreMate is running with the built-in placeholder JWT secret.\n" +
                    " Anyone who has read this project's source can mint valid tokens.\n" +
                    " Set STOREMATE_JWT_SECRET before exposing this server.\n" +
                    "================================================================"
            )
        }
    }

    fun generateToken(username: String): String {
        val now = Date()
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + expirationMs))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun getUsernameFromToken(token: String): String =
        Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token).body.subject

    fun validateToken(token: String): Boolean = try {
        Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
        true
    } catch (ex: JwtException) {
        false
    } catch (ex: IllegalArgumentException) {
        false
    }

    /** Seconds until a freshly minted token expires — handed to clients on login. */
    fun expiresInSeconds(): Long = expirationMs / 1000

    private companion object {
        const val MIN_SECRET_BYTES = 32
        const val PLACEHOLDER_SECRET = "change-me-please-32-characters-min"
    }
}
