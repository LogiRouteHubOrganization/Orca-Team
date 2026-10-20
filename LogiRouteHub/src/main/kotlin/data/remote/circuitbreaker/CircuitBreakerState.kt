package data.remote.circuitbreaker

interface CircuitBreakerState {

    fun checkPermission(
        context: CircuitBreakerSnapshot,
        nowNanos: Long
    ): CircuitBreakerAction

    fun handleSuccess(
        permit: CircuitBreakerExecutionType
    ): CircuitBreakerAction

    fun handleFailure(
        context: CircuitBreakerSnapshot,
        permit: CircuitBreakerExecutionType,
        exception: Exception
    ): CircuitBreakerAction
}
