package domain.usecase.crud.warehouse

import domain.exception.EntityValidationException
import domain.model.Warehouse
import domain.model.updateinput.WarehouseUpdateModel
import domain.repository.WarehouseRepository
import domain.validator.ValidationResult
import domain.validator.rule.IdValidator
import domain.validator.warehouse.UpdateWarehouseValidator

class UpdateWarehouseUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val warehouseValidator: UpdateWarehouseValidator
) {

    suspend operator fun invoke(
        id: String,
        updateRequest: WarehouseUpdateModel
    ): Result<Warehouse> {

        val idValidationResult = IdValidator(
            field = "warehouseId",
            allowedPrefixes = setOf("WH-")
        ).validate(id)

        if (idValidationResult is ValidationResult.Invalid) {
            return Result.failure(
                EntityValidationException(idValidationResult.errors)
            )
        }

        return when (
            val validationResult = warehouseValidator(updateRequest)
        ) {
            ValidationResult.Valid -> warehouseRepository.update(
                    id,
                    updateRequest
                )
            is ValidationResult.Invalid -> Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}

