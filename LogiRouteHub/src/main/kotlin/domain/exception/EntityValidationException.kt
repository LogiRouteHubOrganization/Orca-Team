package domain.exception

import domain.validator.FieldValidator

class EntityValidationException(
    val errors: List<FieldValidator>
) : DomainException(
    message = "Entity validation failed"
)
