package data.remote.exception

import domain.exception.DatabaseConflictException
import domain.exception.DomainException
import domain.exception.NetworkUnavailableException
import domain.exception.ResourceNotFoundException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import java.io.IOException

fun Throwable.toDomainException(
    resourceType: String = "Resource",
    resourceId: String = ""
): DomainException {
    return when (this) {

        is NetworkException.NotFound -> ResourceNotFoundException(
            resourceType = resourceType,
            resourceId = resourceId
        )
        is NetworkException.NoInternet -> NetworkUnavailableException(
            message = this.message ?: "No network connection available",
            cause = this
        )
        is NetworkException.Unauthorized -> DomainException(
            message = this.message ?: "Unauthorized access to resource",
            cause = this
        )
        is NetworkException.ServerError -> DomainException(
            message = this.message ?: "Server error occurred",
            cause = this
        )
        is NetworkException.Unknown -> DomainException(
            message = this.message ?: "Unknown data error occurred",
            cause = this.cause
        )


        is PostgrestRestException -> when {
            statusCode == 404 -> ResourceNotFoundException(resourceType, resourceId)
            statusCode == 409 || code == "23505" -> DatabaseConflictException("Duplicate resource conflict", this)
            else -> DomainException(message ?: "Database error", this)
        }


        is HttpRequestTimeoutException, is IOException -> NetworkUnavailableException(
            message = "Network connection failed or timed out",
            cause = this
        )


        is DomainException -> this


        else -> DomainException(
            message = this.message ?: "An unexpected infrastructure error occurred",
            cause = this
        )
    }
}


private fun mapPostgrestException(
    exception: PostgrestRestException,
    resourceType: String,
    resourceId: String
): DomainException {
    return when (exception.statusCode) {
        404 -> ResourceNotFoundException(
            resourceType = resourceType,
            resourceId = resourceId
        )
        409 -> DatabaseConflictException(
            message = exception.message ?: "Database conflict: duplicate or conflicting resource",
            cause = exception
        )
        else -> DomainException(
            message = exception.message ?: "PostgREST database error (${exception.statusCode})",
            cause = exception
        )
    }
}
