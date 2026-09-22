package data.remote.retry

class RetryLogger {

    fun logAttempt(
        operationName: String,
        currentAttempt: Int,
        totalAttempts: Int
    ) {
        println(
            "[RETRY] $operationName - Attempt " +
                    "$currentAttempt/$totalAttempts"
        )
    }

    fun logRetryableFailure(
        operationName: String,
        currentAttempt: Int,
        totalAttempts: Int,
        exception: Throwable,
        delayMs: Long
    ) {
        println(
            "[RETRY] $operationName - Retryable failure on attempt " +
                    "$currentAttempt/$totalAttempts: ${exception.message}"
        )

        println(
            "[RETRY] $operationName - Backoff delay: ${delayMs}ms"
        )
    }

    fun logNonRetryableFailure(
        operationName: String,
        currentAttempt: Int,
        totalAttempts: Int,
        exception: Throwable
    ) {
        println(
            "[RETRY] $operationName - Non-retryable failure on attempt " +
                    "$currentAttempt/$totalAttempts: ${exception.message}"
        )
    }

    fun logFinalSuccess(
        operationName: String,
        currentAttempt: Int,
        totalAttempts: Int
    ) {
        println(
            "[RETRY] $operationName - Final success on attempt " +
                    "$currentAttempt/$totalAttempts"
        )
    }

    fun logFinalFailure(
        operationName: String,
        currentAttempt: Int,
        totalAttempts: Int,
        exception: Throwable
    ) {
        println(
            "[RETRY] $operationName - Final failure after " +
                    "$currentAttempt/$totalAttempts attempts: " +
                    "${exception.message}"
        )
    }
}
