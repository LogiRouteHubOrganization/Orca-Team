package domain.exception

import domain.validator.FieldViolator

class EntityValidationException(
    val errors: List<FieldViolator>
) : DomainException(
    message = "Entity validation failed"
)
