package domain.validator

sealed class ResultValidator {


    data object Valid : ResultValidator()

    data class Invalid(
        val errors: List<FieldValidator>
    ) : ResultValidator()
}
