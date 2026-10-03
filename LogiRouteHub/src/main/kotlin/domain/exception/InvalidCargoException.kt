package domain.exception

class InvalidCargoException(
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause)
