package data.remote.circuitbreaker

import data.exception.remote.NetworkException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CircuitBreaker(
    private val failureThreshold: Int = DEFAULT_FAILURE_THRESHOLD,
    private val openDurationMs: Long = DEFAULT_OPEN_DURATION_MS
) {

    companion object {
        private const val DEFAULT_FAILURE_THRESHOLD = 5
        private const val DEFAULT_OPEN_DURATION_MS = 5_000L
    }


    private val mutex = Mutex()

    private var currentState: CircuitBreakerState = ClosedState(failureThreshold)
    private var consecutiveFailures = 0
    private var circuitOpenTimestampNanos: Long? = null
    private var isProbeInProgress = false

    init {
        require(failureThreshold > 0)
        { "failureThreshold must be greater than zero" }

        require(openDurationMs > 0)
        { "openDurationMs must be greater than zero" }
    }

    suspend fun <T> execute(block: suspend () -> T): T {

        val executionType = mutex.withLock {
            val snapshot = createSnapshot()
            val action = currentState.checkPermission(snapshot, System.nanoTime())

            when (action) {
                is CircuitBreakerAction.Permit -> action.executionType
                is CircuitBreakerAction.Reject ->
                    throw CallNotPermittedException("Circuit breaker is OPEN or probe in progress.")

                is CircuitBreakerAction.TransitionToClosed -> {
                    transitionTo(ClosedState(failureThreshold))
                    CircuitBreakerExecutionType.NORMAL
                }

                is CircuitBreakerAction.TransitionToOpen -> {
                    transitionTo(OpenState(openDurationMs))
                    throw CallNotPermittedException("Circuit breaker transitioned to OPEN.")
                }

                is CircuitBreakerAction.TransitionToHalfOpen -> {
                    transitionTo(HalfOpenState())
                    isProbeInProgress = true
                    CircuitBreakerExecutionType.PROBE
                }

                is CircuitBreakerAction.RecordFailure -> {
                    CircuitBreakerExecutionType.NORMAL
                }
            }
        }

        return try {
            val result = block()
            handleSuccessResult(executionType)
            result
        } catch (cancellation: CancellationException) {
            handleCancellation(executionType)
            throw cancellation
        } catch (exception: Exception) {
            if (isNetworkFailure(exception)) {
                handleFailureResult(executionType, exception)
            }
            throw exception
        }
    }

    private suspend fun handleSuccessResult(executionType: CircuitBreakerExecutionType) {
        mutex.withLock {
            val action = currentState.handleSuccess(executionType)
            applyAction(action)
        }
    }

    private suspend fun handleFailureResult(executionType: CircuitBreakerExecutionType, exception: Exception) {
        mutex.withLock {
            val snapshot = createSnapshot()
            val action = currentState.handleFailure(snapshot, executionType, exception)
            applyAction(action)
        }
    }

    private suspend fun handleCancellation(executionType: CircuitBreakerExecutionType) {
        mutex.withLock {
            if (executionType == CircuitBreakerExecutionType.PROBE) {
                isProbeInProgress = false
            }
        }
    }

    private fun applyAction(action: CircuitBreakerAction) {
        when (action) {
            is CircuitBreakerAction.TransitionToClosed -> transitionTo(ClosedState(failureThreshold))
            is CircuitBreakerAction.TransitionToOpen -> transitionTo(OpenState(openDurationMs))
            is CircuitBreakerAction.TransitionToHalfOpen -> transitionTo(HalfOpenState())
            is CircuitBreakerAction.RecordFailure -> {
                consecutiveFailures++
            }
            is CircuitBreakerAction.Permit,
            is CircuitBreakerAction.Reject -> {
            }
        }
    }

    private fun transitionTo(newState: CircuitBreakerState) {
        currentState = newState
        when (newState) {
            is ClosedState -> {
                consecutiveFailures = 0
                circuitOpenTimestampNanos = null
                isProbeInProgress = false
            }
            is OpenState -> {
                circuitOpenTimestampNanos = System.nanoTime()
                isProbeInProgress = false
            }
            is HalfOpenState -> {
                isProbeInProgress = true
            }
        }
    }

    private fun createSnapshot(): CircuitBreakerSnapshot {
        return CircuitBreakerSnapshot(
            failureCount = consecutiveFailures,
            circuitOpenTimestampNanos = circuitOpenTimestampNanos,
            isProbeInProgress = isProbeInProgress
        )
    }

    private fun isNetworkFailure(exception: Exception): Boolean {
        return exception is NetworkException.NoInternetException ||
                exception is NetworkException.ServerErrorException ||
                exception is NetworkException.UnknownNetworkException
    }
}
