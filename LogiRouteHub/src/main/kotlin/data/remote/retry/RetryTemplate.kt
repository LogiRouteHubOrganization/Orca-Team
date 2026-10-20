package data.remote.retry

import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlinx.coroutines.CancellationException

abstract class RetryTemplate(
    private val maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
    private val initialDelayMs: Long = DEFAULT_INITIAL_DELAY_MS,
    private val factor: Double = DEFAULT_BACKOFF_FACTOR,
    private val logger: RetryLogger = RetryLogger()
) {

    private companion object {
        const val DEFAULT_MAX_ATTEMPTS = 3
        const val DEFAULT_INITIAL_DELAY_MS = 1000L
        const val DEFAULT_BACKOFF_FACTOR = 2.0

        const val MIN_ATTEMPTS_LIMIT = 0
        const val ATTEMPT_INCREMENT = 1
    }

    suspend fun <T> execute(
        operationName: String,
        block: suspend () -> T
    ): Result<T> {
        require(maxAttempts > MIN_ATTEMPTS_LIMIT) { "maxAttempts must be greater than zero" }

        repeat(maxAttempts) { attemptIndex ->
            val currentAttempt = attemptIndex + ATTEMPT_INCREMENT
            logger.logAttempt(operationName, currentAttempt, maxAttempts)

            try {
                val result = block()
                logger.logFinalSuccess(operationName, currentAttempt, maxAttempts)
                return Result.success(result)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                handleFailure(operationName, currentAttempt, attemptIndex, exception)

                if (isLastAttempt(currentAttempt) || !shouldRetry(exception)) {
                    return Result.failure(exception)
                }

                delay(calculateDelay(attemptIndex))
            }
        }

        error("Retry execution loop terminated without a result or exception for operation: $operationName")
    }

    private fun handleFailure(
        operationName: String,
        currentAttempt: Int,
        attemptIndex: Int,
        exception: Exception
    ) {
        if (isLastAttempt(currentAttempt)) {
            logger.logFinalFailure(operationName, currentAttempt,
                        maxAttempts, exception)

        } else if (!shouldRetry(exception)) {
            logger.logNonRetryableFailure(operationName, currentAttempt,
                        maxAttempts, exception)

        } else {
            val delayMs = calculateDelay(attemptIndex)
            logger.logRetryableFailure(operationName, currentAttempt,
                        maxAttempts, exception, delayMs)
        }
    }

    private fun isLastAttempt(currentAttempt: Int):
            Boolean = currentAttempt == maxAttempts

    private fun calculateDelay(attemptIndex: Int): Long {
        return (initialDelayMs * factor.pow(attemptIndex.toDouble())).toLong()
    }

    protected abstract fun shouldRetry(exception: Throwable): Boolean
}

