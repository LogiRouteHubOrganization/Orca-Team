package domain.validation.common

data class ValidationError(
    val field: String,
    val reason: String
)