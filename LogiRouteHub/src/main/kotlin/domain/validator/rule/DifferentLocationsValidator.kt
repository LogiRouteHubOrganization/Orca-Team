package domain.validator.rule

import domain.validator.FieldViolation
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.Validator

class DifferentLocationsValidator<T> : Validator<Pair<T, T>> {

    override fun validate(
        value: Pair<T, T>
    ): ValidationResult {
        return if (value.first != value.second) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(
                listOf(
                    FieldViolation(field = "values", error = ValidationError.DifferentLocations)
                )
            )
        }
    }
}
