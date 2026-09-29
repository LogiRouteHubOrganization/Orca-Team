package domain.validator.rule

import domain.validator.FieldViolator
import domain.validator.ErrorValidator
import domain.validator.ResultVaildator
import domain.validator.Validator

class PriorityValidator(
    private val field: String,
    private val allowedPriorities: Set<String> = setOf(
        "STANDARD",
        "LOW",
        "URGENT"
    )
) : Validator<String?> {

    override fun validate(value: String?): ResultVaildator {

        if (value.isNullOrBlank()) {
            return ResultVaildator.Invalid(
                listOf(
                    FieldViolator(field = field, error = ErrorValidator.BlankPriority)
                )
            )
        }
        return if (value in allowedPriorities) {
            ResultVaildator.Valid
        } else {
            ResultVaildator.Invalid(
                listOf(
                    FieldViolator(
                        field = field,
                        error = ErrorValidator.InvalidPriority(
                            allowedPriorities = allowedPriorities
                        )
                    )
                )
            )
        }
    }
}
