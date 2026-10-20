package data.remote.retry

import data.exception.remote.NetworkException
import domain.exception.EntityValidationException

class RetryableRemoteDatasource(
    maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
    initialDelayMs: Long = DEFAULT_INITIAL_DELAY_MS,
    factor: Double = DEFAULT_BACKOFF_FACTOR
) : RetryTemplate(
    maxAttempts = maxAttempts,
    initialDelayMs = initialDelayMs,
    factor = factor
) {

    private companion object {
        const val DEFAULT_MAX_ATTEMPTS = 3
        const val DEFAULT_INITIAL_DELAY_MS = 1000L
        const val DEFAULT_BACKOFF_FACTOR = 2.0
    }

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

