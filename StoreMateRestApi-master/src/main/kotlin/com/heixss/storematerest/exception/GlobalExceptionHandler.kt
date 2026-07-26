package com.heixss.storematerest.exception

import com.heixss.storematerest.model.ApiError
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Turns domain failures into meaningful HTTP status codes. Without this every
 * "not found" and "not yours" surfaced to the app as an opaque 500.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(NotFoundException::class, ForeignStoreException::class)
    fun handleNotFound(ex: RuntimeException) =
        error(HttpStatus.NOT_FOUND, ex.message ?: "Not found")

    @ExceptionHandler(InvalidRequestException::class, IllegalArgumentException::class)
    fun handleBadRequest(ex: RuntimeException) =
        error(HttpStatus.BAD_REQUEST, ex.message ?: "Invalid request")

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(ex: ConflictException) =
        error(HttpStatus.CONFLICT, ex.message ?: "Conflict")

    @ExceptionHandler(BadCredentialsError::class)
    fun handleBadCredentials(ex: BadCredentialsError) =
        error(HttpStatus.UNAUTHORIZED, ex.message ?: "Invalid credentials")

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthentication(ex: AuthenticationException) =
        error(HttpStatus.UNAUTHORIZED, ex.message ?: "Authentication required")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val fieldErrors = ex.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "is invalid")
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiError(
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                message = fieldErrors.entries.joinToString("; ") { "${it.key} ${it.value}" }
                    .ifBlank { "Validation failed" },
                fieldErrors = fieldErrors
            )
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ApiError> {
        log.error("Unhandled exception", ex)
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error")
    }

    private fun error(status: HttpStatus, message: String) =
        ResponseEntity.status(status).body(
            ApiError(status = status.value(), error = status.reasonPhrase, message = message)
        )
}

/** Thrown by the auth flow on a username/password mismatch. */
class BadCredentialsError(message: String = "Invalid username or password") : RuntimeException(message)
