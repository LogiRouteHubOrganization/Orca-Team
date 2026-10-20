package data.remote.circuitbreaker

sealed interface CircuitBreakerAction {

    data class Permit(
        val executionType: CircuitBreakerExecutionType
    ) : CircuitBreakerAction

    data object RecordFailure : CircuitBreakerAction

    data object Reject : CircuitBreakerAction

    data object TransitionToClosed : CircuitBreakerAction

    data object TransitionToOpen : CircuitBreakerAction

    data object TransitionToHalfOpen : CircuitBreakerAction
}
