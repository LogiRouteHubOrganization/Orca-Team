package data.exception.remote

import data.exception.remote.NetworkException
import domain.exception.DatabaseConflictException
import domain.exception.DomainException
import domain.exception.NetworkUnavailableException
import domain.exception.ResourceNotFoundException
import domain.exception.ServerErrorException
import domain.exception.UnauthorizedException
import domain.exception.UnknownDomainException
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
        is NetworkException.Unauthorized -> UnauthorizedException(
            message = this.message ?: "Unauthorized access to resource",
            cause = this
        )
        is NetworkException.ServerError -> ServerErrorException(
            message = this.message ?: "Server error occurred",
            cause = this
        )
        is NetworkException.Unknown -> UnknownDomainException(
            message = this.message ?: "Unknown data error occurred",
            cause = this.cause
        )


        is PostgrestRestException -> mapPostgrestException(this, resourceType, resourceId)


        is HttpRequestTimeoutException, is IOException -> NetworkUnavailableException(
            message = "Network connection failed or timed out",
            cause = this
        )


        is DomainException -> this


        else -> UnknownDomainException(
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
    return when {
        exception.statusCode == 404 -> ResourceNotFoundException(
            resourceType = resourceType,
            resourceId = resourceId
        )
        exception.statusCode == 401 || exception.statusCode == 403 -> UnauthorizedException(
            message = exception.message ?: "Unauthorized access to database resource",
            cause = exception
        )
        exception.statusCode == 409 || exception.code == "23505" -> DatabaseConflictException(
            message = exception.message ?: "Database conflict: duplicate or conflicting resource",
            cause = exception
        )
        exception.statusCode in 500..599 -> ServerErrorException(
            message = exception.message ?: "Database server error occurred (${exception.statusCode})",
            cause = exception
        )
        else -> DomainException(
            message = exception.message ?: "PostgREST database error (${exception.statusCode})",
            cause = exception
        )
    }
}
