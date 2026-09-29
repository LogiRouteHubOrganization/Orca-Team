package domain.validator.rule

import domain.validator.FieldValidator
import domain.validator.ErrorValidator
import domain.validator.ResultValidator
import domain.validator.Validator

class DifferentLocationsValidator<T> : Validator<Pair<T, T>> {

    override fun validate(
        value: Pair<T, T>
    ): ResultValidator {
        return if (value.first != value.second) {
            ResultValidator.Valid
        } else {
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(field = "values", error = ErrorValidator.DifferentLocations)
                )
            )
        }
    }
}
