package domain.validator

data class ValidationError(
    val field: String,
    val reason: String
)