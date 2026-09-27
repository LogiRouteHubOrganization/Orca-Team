package data.exception.local

import domain.exception.DomainException
import domain.exception.UnknownDomainException

fun LocalDataException.toDomainException(): DomainException {
    return UnknownDomainException(
        message = this.message ?: "An error occurred while accessing local data storage",
        cause = this
    )
}
