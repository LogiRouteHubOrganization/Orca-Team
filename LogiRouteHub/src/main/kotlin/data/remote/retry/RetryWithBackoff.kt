package data.remote.retry

import kotlinx.coroutines.delay
import kotlin.math.pow

suspend fun <T> retryWithBackoff(
    maxAttempts: Int = 3,
    initialDelayMs: Long = 1000,
    factor: Double = 2.0,
    block: suspend () -> T
): Result<T> {

    require(maxAttempts > 0)
    require(initialDelayMs >= 0)
    require(factor >= 1.0)

    repeat(maxAttempts) { attemptIndex ->

        try {
            return Result.success(block())

        } catch (exception: Exception) {

            val currentAttempt = attemptIndex + 1

            if (currentAttempt == maxAttempts) {
                return Result.failure(exception)
            }

            val delayMs = (initialDelayMs * factor.pow(attemptIndex.toDouble())).toLong()

            delay(delayMs)
        }
    }

    error("Unreachable")
}