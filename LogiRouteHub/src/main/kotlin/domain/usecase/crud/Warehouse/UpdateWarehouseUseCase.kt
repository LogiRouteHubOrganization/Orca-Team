package domain.usecase.crud.warehouse

import domain.exception.EntityValidationException
import domain.model.Warehouse
import domain.model.updateinput.WarehouseUpdateModel
import domain.repository.WarehouseRepository
import domain.validator.ValidationResult
import domain.validator.warehouse.UpdateWarehouseValidator

class UpdateWarehouseUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val warehouseValidator: UpdateWarehouseValidator
) {

    suspend operator fun invoke(
        id: String,
        updateRequest: WarehouseUpdateModel
    ): Result<Warehouse> {

        return when (
            val validationResult = warehouseValidator(updateRequest)
        ) {
            ValidationResult.Success ->
                warehouseRepository.update(
                    id,
                    updateRequest
                )

            is ValidationResult.Failure ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}