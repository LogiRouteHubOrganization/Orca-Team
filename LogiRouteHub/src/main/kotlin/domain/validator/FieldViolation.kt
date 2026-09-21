package domain.validator

data class FieldViolation(
    val field: String,
    val error: ValidationError
)
