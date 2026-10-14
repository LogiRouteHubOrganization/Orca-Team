package domain.usecase

import domain.exception.EntityValidationException
import domain.extension.runCatchingCancellable
import domain.model.Warehouse
import domain.model.updateinput.VehicleUpdateModel
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository
import domain.validator.ResultValidator
import domain.validator.vehicle.UpdateVehicleValidator
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class AddVehicleToHubUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val vehicleRepository: VehicleRepository,
    private val updateVehicleValidator: UpdateVehicleValidator
) {

    suspend operator fun invoke(
        warehouseId: String,
        vehicleId: String
    ): Result<Warehouse?> = runCatchingCancellable {

        val (warehouse, vehicle) = coroutineScope {
            val warehouseDeferred = async { warehouseRepository.findById(warehouseId).getOrThrow() }
            val vehicleDeferred = async { vehicleRepository.findById(vehicleId).getOrThrow() }

            Pair(warehouseDeferred.await(), vehicleDeferred.await())
        }

        if (warehouse == null || vehicle == null) {
            return@runCatchingCancellable null
        }

        val vehicleUpdateModel = VehicleUpdateModel(
            currentWarehouseId = warehouse.id
        )

        val validationResult = updateVehicleValidator(vehicleUpdateModel)

        if (validationResult is ResultValidator.Invalid) {
            throw EntityValidationException(
                validationResult.errors
            )
        }

        val updatedVehicle = vehicleRepository.update(vehicle.id, vehicleUpdateModel)
            .getOrThrow()


        warehouse.addVehicle(updatedVehicle)
        warehouse
    }
}
