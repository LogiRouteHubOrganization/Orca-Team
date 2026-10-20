package data.remote.circuitbreaker

class CallNotPermittedException(
    message: String = "Circuit breaker is OPEN. Call is not permitted."
) : RuntimeException(message)
