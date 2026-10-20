package data.remote.circuitbreaker


data class CircuitBreakerSnapshot(
    val failureCount: Int,
    val circuitOpenTimestampNanos: Long?,
    val isProbeInProgress: Boolean
)
