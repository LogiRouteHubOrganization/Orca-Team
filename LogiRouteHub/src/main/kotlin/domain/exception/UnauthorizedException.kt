package domain.exception

class UnauthorizedException(
    message: String = "Unauthorized access. Please check credentials.",
    cause: Throwable? = null
) : DomainException(message, cause)
