package domain.exception

sealed class DomainException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

class NetworkUnavailableException(
    message: String = "Network is unavailable",
    cause: Throwable? = null
) : DomainException(message, cause)

class EntityValidationException(
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause)

class ResourceNotFoundException(
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause)

class DatabaseConflictException(
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause)