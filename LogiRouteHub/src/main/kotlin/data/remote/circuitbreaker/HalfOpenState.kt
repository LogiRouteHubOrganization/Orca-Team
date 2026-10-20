package data.remote.circuitbreaker

class HalfOpenState : CircuitBreakerState {

    override fun checkPermission(
        context: CircuitBreakerSnapshot,
        nowNanos: Long
    ): CircuitBreakerAction {
        if (context.isProbeInProgress) {
            return CircuitBreakerAction.Reject
        }

        return CircuitBreakerAction.Permit(
            CircuitBreakerExecutionType.PROBE
        )
    }

    override fun handleSuccess(
        permit: CircuitBreakerExecutionType
    ): CircuitBreakerAction {
        return when (permit) {
            CircuitBreakerExecutionType.PROBE -> CircuitBreakerAction.TransitionToClosed
            CircuitBreakerExecutionType.NORMAL -> CircuitBreakerAction.TransitionToClosed
        }
    }

    override fun handleFailure(
        context: CircuitBreakerSnapshot,
        permit: CircuitBreakerExecutionType,
        exception: Exception
    ): CircuitBreakerAction {
        return CircuitBreakerAction.TransitionToOpen
    }
}
