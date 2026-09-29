package domain.validator.rule

import domain.validator.FieldValidator
import domain.validator.ErrorValidator
import domain.validator.ResultValidator
import domain.validator.Validator

class PriorityValidator(
    private val field: String,
    private val allowedPriorities: Set<String> = setOf(
        "STANDARD",
        "LOW",
        "URGENT"
    )
) : Validator<String?> {

    override fun validate(value: String?): ResultValidator {

        if (value.isNullOrBlank()) {
            return ResultValidator.Invalid(
                listOf(
                    FieldValidator(field = field, error = ErrorValidator.BlankPriority)
                )
            )
        }
        return if (value in allowedPriorities) {
            ResultValidator.Valid
        } else {
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
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
