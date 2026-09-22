package data.remote.retry

import data.exception.remote.NetworkException
import domain.exception.EntityValidationException

class NetworkRetryTemplate(
    maxAttempts: Int = 3,
    initialDelayMs: Long = 1000,
    factor: Double = 2.0
) : RetryTemplate(
    maxAttempts = maxAttempts,
    initialDelayMs = initialDelayMs,
    factor = factor
) {

    override fun shouldRetry(
        exception: Throwable
    ): Boolean {
        return when (exception) {
            is NetworkException.NoInternetException -> true
            is NetworkException.ServerErrorException -> true

            is EntityValidationException -> false
            is NetworkException.UnauthorizedException -> false
            is NetworkException.NotFoundException -> false
            is NetworkException.UnprocessableEntityException -> false
            is NetworkException.UnknownNetworkException -> false

            else -> false
        }
    }
}
