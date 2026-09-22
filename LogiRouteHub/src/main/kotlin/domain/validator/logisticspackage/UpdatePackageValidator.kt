package domain.validator.logisticspackage

import domain.model.updateinput.PackageUpdateModel
import domain.validator.FieldViolation
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.rule.IdValidator
import domain.validator.rule.PositiveNumberValidator
import domain.validator.rule.PriorityValidator

class UpdatePackageValidator {

    operator fun invoke(updateModel: PackageUpdateModel): ValidationResult {
        if (!hasAnyUpdate(updateModel)) {
            return ValidationResult.Failure(
                listOf(FieldViolation(field = "update", error = ValidationError.EmptyUpdate))
            )
        }

        val whPrefix = setOf("WH-")
        val results = listOfNotNull(
            updateModel.weightByKg?.let {
                PositiveNumberValidator(field = "weightByKg").validate(it)
            },
            updateModel.priority?.let {
                PriorityValidator(field = "priority").validate(it)
            },
            updateModel.originWarehouseId?.let {
                IdValidator(field = "originWarehouseId", allowedPrefixes = whPrefix).validate(it)
            },
            updateModel.destinationWarehouseId?.let {
                IdValidator(field = "destinationWarehouseId", allowedPrefixes = whPrefix).validate(it)
            }
        )

        val errors = results.filterIsInstance<ValidationResult.Failure>().flatMap { it.errors }
        return if (errors.isEmpty()) ValidationResult.Success else ValidationResult.Failure(errors)
    }

    private fun hasAnyUpdate(updateModel: PackageUpdateModel): Boolean {
        return listOf(
            updateModel.weightByKg,
            updateModel.priority,
            updateModel.originWarehouseId,
            updateModel.destinationWarehouseId
        ).any { it != null }
    }
}
