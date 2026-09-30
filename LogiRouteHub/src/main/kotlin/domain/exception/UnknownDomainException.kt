package domain.exception

class UnknownDomainException(
    message: String = "An unexpected error occurred.",
    cause: Throwable? = null
) : DomainException(message, cause)
