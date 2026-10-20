package data.remote.circuitbreaker

class ClosedState(
    private val failureThreshold: Int
) : CircuitBreakerState {

    override fun checkPermission(
        context: CircuitBreakerSnapshot,
        nowNanos: Long
    ): CircuitBreakerAction {
        return CircuitBreakerAction.Permit(
            CircuitBreakerExecutionType.NORMAL
        )
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

        val nextFailureCount = context.failureCount + 1

        return if (nextFailureCount >= failureThreshold) {
            CircuitBreakerAction.TransitionToOpen
        } else {
            CircuitBreakerAction.RecordFailure
        }
    }
}
