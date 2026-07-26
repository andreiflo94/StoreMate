package com.example.storemate.data.remote

import com.example.storemate.data.remote.dto.ApiErrorDto
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** Signals that the stored token was rejected, so the app must sign out. */
class SessionExpiredException(message: String) : Exception(message)

/** Signals that the server could not be reached at all. */
class ServerUnreachableException(message: String) : Exception(message)

private val errorJson = Json { ignoreUnknownKeys = true }

/**
 * Turns transport and HTTP failures into something worth showing in a snackbar.
 *
 * Shop staff cannot act on "HTTP 500", but they can act on "can't reach the
 * server — check that the shop PC is on".
 */
fun Throwable.toUserFacingException(): Exception = when (this) {
    is HttpException -> when (code()) {
        401 -> SessionExpiredException(serverMessage() ?: "Your session expired — please sign in again")
        403 -> Exception(serverMessage() ?: "You do not have access to that")
        404 -> Exception(serverMessage() ?: "Not found on the server")
        in 500..599 -> Exception(serverMessage() ?: "The server hit an error (${code()})")
        else -> Exception(serverMessage() ?: "Request failed (${code()})")
    }

    is UnknownHostException ->
        ServerUnreachableException("Can't find that server address — check the address and your Wi-Fi")

    is ConnectException ->
        ServerUnreachableException("Can't reach the StoreMate server — is it running?")

    is SocketTimeoutException ->
        ServerUnreachableException("The server took too long to respond")

    is IOException ->
        ServerUnreachableException(message ?: "Network error — check your connection")

    is Exception -> this
    else -> Exception(message ?: "Unexpected error")
}

/** Reads the `message` field out of the backend's JSON error body, if present. */
private fun HttpException.serverMessage(): String? = try {
    response()?.errorBody()?.string()
        ?.takeIf { it.isNotBlank() }
        ?.let { errorJson.decodeFromString<ApiErrorDto>(it).message }
        ?.takeIf { it.isNotBlank() }
} catch (ex: Exception) {
    null
}
