package data.exception.remote

import domain.exception.DomainException
import domain.exception.EntityConflictException
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

        is NetworkException.NotFoundException -> mapToSpecificNotFoundException(resourceType, resourceId)

        is NetworkException.NoInternetException -> NetworkUnavailableException(
            message = this.message ?: "No network connection available",
            cause = this
        )
        is NetworkException.UnauthorizedException -> UnauthorizedException(
            message = this.message ?: "Unauthorized access to resource",
            cause = this
        )
        is NetworkException.ServerErrorException -> ServerErrorException(
            message = this.message ?: "Server error occurred",
            cause = this
        )
        is NetworkException.UnknownNetworkException -> UnknownDomainException(
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
        exception.statusCode == 404 -> mapToSpecificNotFoundException(resourceType, resourceId)

        exception.statusCode == 401 || exception.statusCode == 403 -> UnauthorizedException(
            message = exception.message ?: "Unauthorized access to database resource",
            cause = exception
        )
        exception.statusCode == 409 || exception.code == "23505" -> EntityConflictException(
            message = exception.message ?: "Resource conflict: duplicate or conflicting entry",
            cause = exception
        )
        exception.statusCode in 500..599 -> ServerErrorException(
            message = exception.message ?: "Database server error occurred (${exception.statusCode})",
            cause = exception
        )
        else -> UnknownDomainException(
            message = exception.message ?: "PostgREST database error (${exception.statusCode})",
            cause = exception
        )
    }
}


private fun mapToSpecificNotFoundException(
    resourceType: String,
    resourceId: String
): ResourceNotFoundException {
    return when (resourceType.lowercase()) {
        "route" -> ResourceNotFoundException.RouteNotFound(resourceId)
        "vehicle" -> ResourceNotFoundException.VehicleNotFound(resourceId)
        "warehouse" -> ResourceNotFoundException.WarehouseNotFound(resourceId)
        "package" -> ResourceNotFoundException.PackageNotFound(resourceId)
        else -> ResourceNotFoundException.Default(resourceType, resourceId)
    }
}
