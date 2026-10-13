
package data.remote.utils

import data.exception.remote.NetworkException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> safeApiCall(
    block: suspend () -> T
): T {
    return try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: RestException) {

        when (e.statusCode) {
            401, 403 -> throw NetworkException.UnauthorizedException(
                e.message ?: "Unauthorized access"
            )

            404 -> throw NetworkException.NotFoundException(
                e.message ?: "Resource not found"
            )

            422 -> throw NetworkException.UnprocessableEntityException(
                e.message ?: "Unprocessable entity"
            )

            in 500..599 -> throw NetworkException.ServerErrorException(
                e.message ?: "Server error"
            )

            else -> throw NetworkException.UnknownNetworkException(
                e.message ?: "Unknown Supabase error",
                e
            )
        }
    } catch (e: UnknownHostException) {
        throw NetworkException.NoInternetException(
            "No internet connection available"
        )
    } catch (e: HttpRequestTimeoutException) {
        throw NetworkException.NoInternetException(
            "Request timed out"
        )
    } catch (e: Exception) {
        throw NetworkException.UnknownNetworkException(
            e.message ?: "An unexpected error occurred",
            e
        )
    }
}
