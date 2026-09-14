package domain.usecase.crud.warehouse

import domain.exception.EntityValidationException
import domain.exception.ResourceNotFoundException
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.validator.ValidationResult
import domain.validator.WarehouseValidator

class UpdateWarehouseUseCase(
    private val warehouseRepository: WarehouseRepository
) {

    suspend operator fun invoke(
        id: String,
        name: String? = null,
        regionalZone: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<Unit> {

        val validation = WarehouseValidator.validateUpdate(
            id = id,
            name = name,
            regionalZone = regionalZone,
            latitude = latitude,
            longitude = longitude
        )

        if (validation is ValidationResult.Invalid) {
            return Result.failure(
                EntityValidationException(
                    validation.violations.joinToString("; ") {
                        "${it.field}: ${it.message}"
                    }
                )
            )
        }

        val currentWarehouse = warehouseRepository.findById(id)
            ?: return Result.failure(
                ResourceNotFoundException(
                    "Warehouse not found: $id"
                )
            )

        val updatedWarehouse = Warehouse(
            id = currentWarehouse.id,
            name = name ?: currentWarehouse.name,
            regionalZone = regionalZone ?: currentWarehouse.regionalZone,
            latitude = latitude ?: currentWarehouse.latitude,
            longitude = longitude ?: currentWarehouse.longitude
        )

        // Preserve existing warehouse relationships/state.
        currentWarehouse.cargoQueue.forEach {
            updatedWarehouse.addPackage(it)
        }

        currentWarehouse.outgoingRoutes.forEach {
            updatedWarehouse.addRoute(it)
        }

        currentWarehouse.stationedVehicles.forEach {
            updatedWarehouse.addVehicle(it)
        }

        warehouseRepository.update(updatedWarehouse)

        return Result.success(Unit)
    }
}