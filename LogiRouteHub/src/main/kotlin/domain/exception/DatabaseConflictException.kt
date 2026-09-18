package domain.exception

class DatabaseConflictException(
    message: String = "A database conflict occurred",
    cause: Throwable? = null
) : DomainException(message, cause)
