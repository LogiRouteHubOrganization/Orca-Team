package data.remote.retry

class RetryLogger {

    private fun logMessage(message: String) {
        println("[RETRY] $message")
    }

    fun logAttempt(operationName: String, currentAttempt: Int, totalAttempts: Int) {
        logMessage("$operationName - Attempt $currentAttempt/$totalAttempts")
    }

    fun logRetryableFailure(
        operationName: String,
        currentAttempt: Int,
        totalAttempts: Int,
        exception: Throwable,
        delayMs: Long
    ) {
        logMessage("$operationName - Retryable failure on attempt $currentAttempt/$totalAttempts: ${exception.message}")
        logMessage("$operationName - Backoff delay: ${delayMs}ms")
    }

    fun logNonRetryableFailure(
        operationName: String,
        currentAttempt: Int,
        totalAttempts: Int,
        exception: Throwable
    ) {
        logMessage("$operationName - Non-retryable failure on attempt $currentAttempt/$totalAttempts: ${exception.message}")
    }

    fun logFinalSuccess(operationName: String, currentAttempt: Int, totalAttempts: Int) {
        logMessage("$operationName - Final success on attempt $currentAttempt/$totalAttempts")
    }

    fun logFinalFailure(
        operationName: String,
        currentAttempt: Int,
        totalAttempts: Int,
        exception: Throwable
    ) {
        logMessage("$operationName - Final failure after $currentAttempt/$totalAttempts attempts: ${exception.message}")
    }
}

