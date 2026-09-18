package domain.usecase.crud.warehouse

import domain.exception.EntityValidationException
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.validator.ValidationResult
import domain.validator.warehouse.CreateWarehouseValidator

class CreateWarehouseUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val warehouseValidator: CreateWarehouseValidator
) {

    suspend operator fun invoke(
        warehouse: Warehouse
    ): Result<Warehouse> {

        return when (
            val validationResult = warehouseValidator(
                id = warehouse.id,
                name = warehouse.name,
                regionalZone = warehouse.regionalZone,
                latitude = warehouse.latitude,
                longitude = warehouse.longitude
            )
        ) {
            ValidationResult.Success ->
                warehouseRepository.add(warehouse)

            is ValidationResult.Failure ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}