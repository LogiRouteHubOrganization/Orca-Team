package data.remote.retry

class RetryLogger {

fun logAttempt(
        currentAttempt: Int,
        totalAttempts: Int
    ) {
    println("[RETRY] Attempt $currentAttempt/$totalAttempts")
    }

    fun logRetryableFailure(
        currentAttempt: Int,
        totalAttempts: Int,
        exception: Throwable,
        delayMs: Long
    ) {
        println("[RETRY] Retryable failure on attempt " +"$currentAttempt/$totalAttempts: ${exception.message}")
        println("[RETRY] Backoff delay: ${delayMs}ms")
    }

fun logNonRetryableFailure(
        currentAttempt: Int,
        totalAttempts: Int,
        exception: Throwable
    ) {
        println("[RETRY] Non-retryable failure on attempt " + "$currentAttempt/$totalAttempts: ${exception.message}")
    }

fun logFinalSuccess(
        currentAttempt: Int,
        totalAttempts: Int
    ) {
        println(
            "[RETRY] Final success on attempt " +
                    "$currentAttempt/$totalAttempts"
        )
    }

fun logFinalFailure(
        currentAttempt: Int,
        totalAttempts: Int,
        exception: Throwable
    ) {
        println("[RETRY] Final failure after " + "$currentAttempt/$totalAttempts attempts: " +
        "${exception.message}")
    }
}