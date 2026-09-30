package domain.validator

data class FieldValidator(
    val field: String,
    val error: ErrorValidator
)
