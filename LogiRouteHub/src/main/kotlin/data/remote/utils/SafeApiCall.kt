package data.remote.utils

import data.remote.exception.NetworkException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import java.net.UnknownHostException

suspend fun <T> safeApiCall(
    block: suspend () -> T
): T {
    return try {
        block()
    } catch (e: RestException) {
        when (e.statusCode) {
            400, 422 -> throw NetworkException.Unknown(
                e.message ?: "Invalid request"
            )

            401, 403 -> throw NetworkException.Unauthorized(
                e.message ?: "Unauthorized access"
            )

            404 -> throw NetworkException.NotFound(
                e.message ?: "Resource not found"
            )

            in 500..599 -> throw NetworkException.ServerError(
                e.message ?: "Server error"
            )

            else -> throw NetworkException.Unknown(
                e.message ?: "Unknown Supabase error",
                e
            )
        }
    } catch (e: UnknownHostException) {
        throw NetworkException.NoInternet(
            "No internet connection available"
        )
    } catch (e: HttpRequestTimeoutException) {
        throw NetworkException.NoInternet(
            "Request timed out"
        )
    } catch (e: Exception) {
        throw NetworkException.Unknown(
            e.message ?: "An unexpected error occurred",
            e
        )
    }
}