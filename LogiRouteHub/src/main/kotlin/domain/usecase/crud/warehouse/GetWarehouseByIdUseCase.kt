package domain.usecase.crud.warehouse

import domain.exception.EntityValidationException
import domain.exception.ResourceNotFoundException
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.validator.FieldViolation
import domain.validator.WarehouseValidator

class GetWarehouseByIdUseCase(
    private val warehouseRepository: WarehouseRepository
) {

    suspend operator fun invoke(
        warehouseId: String
    ): Result<Warehouse> {

        val violations = mutableListOf<FieldViolation>()

        WarehouseValidator.validateId(
            id = warehouseId,
            violations = violations
        )

        if (violations.isNotEmpty()) {
            return Result.failure(
                EntityValidationException(
                    violations.joinToString("; ") {
                        "${it.field}: ${it.message}"
                    }
                )
            )
        }

        val warehouse = warehouseRepository.findById(warehouseId)

        return if (warehouse != null) {
            Result.success(warehouse)
        } else {
            Result.failure(
                ResourceNotFoundException(
                    "Warehouse not found: $warehouseId"
                )
            )
        }
    }
}