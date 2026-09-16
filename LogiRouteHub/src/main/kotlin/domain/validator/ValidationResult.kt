package domain.validator

sealed class ValidationResult {

    data object Success : ValidationResult()

    data class Failure(
        val errors: List<ValidationError>
    ) : ValidationResult()
}