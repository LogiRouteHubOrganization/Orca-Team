package domain.exception

class EntityConflictException(
    message: String = "A resource conflict occurred",
    cause: Throwable? = null
) : DomainException(message, cause)
