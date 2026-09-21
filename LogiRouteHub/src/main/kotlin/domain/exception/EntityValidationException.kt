package domain.exception

import domain.validator.FieldViolation

class EntityValidationException(
    val errors: List<FieldViolation>
) : DomainException(
    message = "Entity validation failed"
)
