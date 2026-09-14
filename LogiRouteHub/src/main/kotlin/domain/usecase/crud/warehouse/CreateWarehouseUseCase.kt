package domain.usecase.crud.warehouse

import domain.exception.EntityValidationException
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.validator.ValidationResult
import domain.validator.WarehouseValidator

class CreateWarehouseUseCase(
    private val warehouseRepository: WarehouseRepository
) {

    suspend operator fun invoke(warehouse: Warehouse): Result<Unit> {

        return when (val validation = WarehouseValidator.validateCreate(warehouse)) {
            ValidationResult.Valid -> {
                warehouseRepository.add(warehouse)
                Result.success(Unit)
            }

            is ValidationResult.Invalid -> {
                Result.failure(
                    EntityValidationException(
                        validation.violations.joinToString("; ") {
                            "${it.field}: ${it.message}"
                        }
                    )
                )
            }
        }
    }
}