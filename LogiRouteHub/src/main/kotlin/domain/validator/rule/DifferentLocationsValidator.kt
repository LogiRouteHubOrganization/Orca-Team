package domain.validator.rule

import domain.validator.FieldViolator
import domain.validator.ErrorValidator
import domain.validator.ResultVaildator
import domain.validator.Validator

class DifferentLocationsValidator<T> : Validator<Pair<T, T>> {

    override fun validate(
        value: Pair<T, T>
    ): ResultVaildator {
        return if (value.first != value.second) {
            ResultVaildator.Valid
        } else {
            ResultVaildator.Invalid(
                listOf(
                    FieldViolator(field = "values", error = ErrorValidator.DifferentLocations)
                )
            )
        }
    }
}
