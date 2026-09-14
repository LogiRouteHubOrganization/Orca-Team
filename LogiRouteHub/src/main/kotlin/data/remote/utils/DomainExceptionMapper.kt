package data.remote.utils

import data.remote.exception.NetworkException
import domain.exception.DatabaseConflictException
import domain.exception.NetworkUnavailableException
import domain.exception.ResourceNotFoundException

fun Throwable.toDomainException(): Throwable {
    return when (this) {

        is NetworkException.NoInternet ->
            NetworkUnavailableException(
                message = message ?: "Network is unavailable",
                cause = this
            )

        is NetworkException.NotFound ->
            ResourceNotFoundException(
                message = message ?: "Resource not found",
                cause = this
            )

        is NetworkException.Unknown -> {
            if (message?.contains("409") == true ||
                message?.contains("conflict", ignoreCase = true) == true
            ) {
                DatabaseConflictException(
                    message = message ?: "Database conflict",
                    cause = this
                )
            } else {
                this
            }
        }

        else -> this
    }
}