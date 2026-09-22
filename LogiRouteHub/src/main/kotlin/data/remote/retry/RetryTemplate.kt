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
        block: suspend () -> T
    ): Result<T> {
        repeat(maxAttempts) { attemptIndex ->
            val firstAttemptNumber = 1
            val currentAttempt = attemptIndex +firstAttemptNumber

            logger.logAttempt(
                currentAttempt,
                maxAttempts
            )

            try {

                val result = block()

                logger.logFinalSuccess(
                    currentAttempt,
                    maxAttempts
                )

                return Result.success(result)

            } catch (exception: Exception) {

                val isLastAttempt = currentAttempt == maxAttempts

                if (isLastAttempt) {

                    logger.logFinalFailure(
                        currentAttempt,
                        maxAttempts,
                        exception
                    )

                    return Result.failure(exception)
                }

                if (!shouldRetry(exception)) {

                    logger.logNonRetryableFailure(
                        currentAttempt,
                        maxAttempts,
                        exception
                    )

                    return Result.failure(exception)
                }

                val delayMs = (
                        initialDelayMs *
                                factor.pow(attemptIndex.toDouble())
                        ).toLong()

                logger.logRetryableFailure(
                    currentAttempt,
                    maxAttempts,
                    exception,
                    delayMs
                )

                delay(delayMs)
            }
        }

        error("Retry execution ended unexpectedly")
    }

    protected abstract fun shouldRetry(
        exception: Throwable
    ): Boolean
}