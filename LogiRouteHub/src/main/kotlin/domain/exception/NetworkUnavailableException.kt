package domain.exception

class NetworkUnavailableException(
    message: String = "Network is unavailable",
    cause: Throwable? = null
) : DomainException(message, cause)
