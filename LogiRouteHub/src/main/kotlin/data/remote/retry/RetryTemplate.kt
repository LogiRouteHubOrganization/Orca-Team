package data.remote.retry

import kotlinx.coroutines.delay
import kotlin.math.pow

abstract class RetryTemplate(
    private val maxAttempts: Int = 3,
    private val initialDelayMs: Long = 1000,
    private val factor: Double = 2.0
) {
    suspend fun <T> execute(
        block: suspend () -> T
    ): Result<T> {

        repeat(maxAttempts) { attemptIndex ->

            try {
                return Result.success(block())

            } catch (exception: Exception) {

                val currentAttempt = attemptIndex + 1

                if (currentAttempt == maxAttempts) {
                    return Result.failure(exception)
                }

                if (!shouldRetry(exception)) {
                    return Result.failure(exception)
                }

                val delayMs = (initialDelayMs * factor.pow(attemptIndex.toDouble())).toLong()

                delay(delayMs)
            }
        }
        error("Unreachable")
    }

    protected abstract fun shouldRetry(
        exception: Throwable
    ): Boolean
}
