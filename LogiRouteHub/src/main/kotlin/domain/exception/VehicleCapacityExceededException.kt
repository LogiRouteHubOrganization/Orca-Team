package domain.exception

class VehicleCapacityExceededException(
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause)

