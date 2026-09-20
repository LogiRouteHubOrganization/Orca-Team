package domain.exception

class ServerErrorException(
    message: String = "Internal server error occurred.",
    cause: Throwable? = null
) : DomainException(message, cause)
