package data.remote.circuitbreaker

class OpenState(
    private val openDurationMs: Long
) : CircuitBreakerState {

    companion object {
        private const val NANOS_PER_MILLISECOND = 1_000_000L
    }

    override fun checkPermission(
        context: CircuitBreakerSnapshot,
        nowNanos: Long
    ): CircuitBreakerAction {
        if (isResetTimeoutExpired(context.circuitOpenTimestampNanos, nowNanos)) {
            return CircuitBreakerAction.TransitionToHalfOpen
        }
        return CircuitBreakerAction.Reject
    }

    override fun handleSuccess(
        permit: CircuitBreakerExecutionType
    ): CircuitBreakerAction {
        return CircuitBreakerAction.TransitionToClosed
    }

    override fun handleFailure(
        context: CircuitBreakerSnapshot,
        permit: CircuitBreakerExecutionType,
        exception: Exception
    ): CircuitBreakerAction {
        return CircuitBreakerAction.TransitionToOpen
    }

    private fun isResetTimeoutExpired(
        openTimestampNanos: Long?,
        nowNanos: Long
    ): Boolean {
        val openedAt = openTimestampNanos ?: return false
        val elapsedNanos = nowNanos - openedAt
        val timeoutNanos = openDurationMs * NANOS_PER_MILLISECOND

        return elapsedNanos >= timeoutNanos
    }
}
