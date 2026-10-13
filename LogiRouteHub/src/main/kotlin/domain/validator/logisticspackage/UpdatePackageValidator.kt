package domain.validator.logisticspackage

import domain.model.updateinput.PackageUpdateModel
import domain.validator.FieldValidator
import domain.validator.ErrorValidator
import domain.validator.ResultValidator
import domain.validator.rule.IdValidator
import domain.validator.rule.PositiveNumberValidator
import domain.validator.rule.PriorityValidator

class UpdatePackageValidator {

    operator fun invoke(updateModel: PackageUpdateModel): ResultValidator {
        if (!hasAnyUpdate(updateModel)) {
            return ResultValidator.Invalid(
                listOf(FieldValidator(field = "update", error = ErrorValidator.EmptyUpdate))
            )
        }

        val whPrefix = setOf("WH-")
        val results = listOfNotNull(
            updateModel.weightByKg?.let {
                PositiveNumberValidator(field = "weightByKg").validate(it)
            },
            updateModel.volumeM3?.let {
                PositiveNumberValidator(field = "volumeM3").validate(it)
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
        val errors = results.filterIsInstance<ResultValidator.Invalid>().flatMap { it.errors }
        return if (errors.isEmpty()) ResultValidator.Valid else ResultValidator.Invalid(errors)
    }
    private fun hasAnyUpdate(updateModel: PackageUpdateModel): Boolean {
        return listOf(
            updateModel.weightByKg,
            updateModel.volumeM3,
            updateModel.priority,
            updateModel.originWarehouseId,
            updateModel.destinationWarehouseId
        ).any { it != null }
    }
}
