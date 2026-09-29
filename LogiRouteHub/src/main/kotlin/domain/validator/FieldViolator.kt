package domain.validator

data class FieldViolator(
    val field: String,
    val error: ErrorValidator
)
