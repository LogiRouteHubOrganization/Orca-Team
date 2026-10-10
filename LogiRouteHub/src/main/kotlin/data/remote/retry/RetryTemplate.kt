package data.remote.retry

import kotlinx.coroutines.delay
import kotlin.math.pow

abstract class RetryTemplate(
    private val maxAttempts: Int = 3,
    private val initialDelayMs: Long = 1000,
    private val factor: Double = 2.0,
    private val logger: RetryLogger = RetryLogger()
) {

    suspend fun <T> execute(
        operationName: String,
        block: suspend () -> T
    ): Result<T> {
        repeat(maxAttempts) { attemptIndex ->
            val currentAttempt = attemptIndex + 1
            logger.logAttempt(operationName, currentAttempt, maxAttempts)

            try {
                val result = block()
                logger.logFinalSuccess(operationName, currentAttempt, maxAttempts)
                return Result.success(result)
            } catch (exception: Exception) {
                logException(operationName, currentAttempt, attemptIndex, exception)
                if (shouldStopRetry(currentAttempt, exception)) {
                    return Result.failure(exception)
                }
            }
        }
        error("Retry execution ended unexpectedly")
    }

    private fun logException(
        operationName: String,
        currentAttempt: Int,
        attemptIndex: Int,
        exception: Exception
    ) {
        val isLastAttempt = currentAttempt == maxAttempts

        if (isLastAttempt) {
            logger.logFinalFailure(operationName, currentAttempt, maxAttempts, exception)
        } else if (!shouldRetry(exception)) {
            logger.logNonRetryableFailure(operationName, currentAttempt, maxAttempts, exception)
        } else {
            val delayMs = calculateDelay(attemptIndex)
            logger.logRetryableFailure(operationName, currentAttempt, maxAttempts, exception, delayMs)
        }
    }

    private fun shouldStopRetry(
        currentAttempt: Int,
        exception: Exception
    ): Boolean {
        val isLastAttempt = currentAttempt == maxAttempts
        return isLastAttempt || !shouldRetry(exception)
    }

    private fun calculateDelay(attemptIndex: Int): Long {
        return (initialDelayMs * factor.pow(attemptIndex.toDouble())).toLong()
    }

    protected abstract fun shouldRetry(
        exception: Throwable
    ): Boolean
}
