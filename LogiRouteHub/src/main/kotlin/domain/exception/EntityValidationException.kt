package domain.exception

import domain.validator.ValidationError

class EntityValidationException(
    val errors: List<ValidationError>
) : DomainException(
    message = errors.joinToString("; ") { error ->
        "${error.field}: ${error.reason}"
    }
)
