package domain.validator.logisticspackage

import domain.model.updateinput.PackageUpdateModel
import domain.validator.FieldViolator
import domain.validator.ErrorValidator
import domain.validator.ResultVaildator
import domain.validator.rule.IdValidator
import domain.validator.rule.PositiveNumberValidator
import domain.validator.rule.PriorityValidator

class UpdatePackageValidator {

    operator fun invoke(updateModel: PackageUpdateModel): ResultVaildator {
        if (!hasAnyUpdate(updateModel)) {
            return ResultVaildator.Invalid(
                listOf(FieldViolator(field = "update", error = ErrorValidator.EmptyUpdate))
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

        val errors = results.filterIsInstance<ResultVaildator.Invalid>().flatMap { it.errors }
        return if (errors.isEmpty()) ResultVaildator.Valid else ResultVaildator.Invalid(errors)
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
