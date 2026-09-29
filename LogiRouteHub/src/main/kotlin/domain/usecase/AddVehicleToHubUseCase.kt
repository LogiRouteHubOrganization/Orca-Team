package domain.usecase

import domain.exception.EntityValidationException
import domain.model.Warehouse
import domain.model.updateinput.VehicleUpdateModel
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository
import domain.validator.ResultVaildator
import domain.validator.vehicle.UpdateVehicleValidator

class AddVehicleToHubUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val vehicleRepository: VehicleRepository,
    private val updateVehicleValidator: UpdateVehicleValidator
) {

    suspend operator fun invoke(
        warehouseId: String,
        vehicleId: String
    ): Result<Warehouse?> = runCatching {

        val warehouse = warehouseRepository.findById(warehouseId).getOrThrow() ?: return@runCatching null

        val vehicle = vehicleRepository.findById(vehicleId).getOrThrow() ?: return@runCatching null

        val vehicleUpdateModel = VehicleUpdateModel(
            currentWarehouseId = warehouse.id
        )

        val validationResult = updateVehicleValidator(vehicleUpdateModel)

        if (validationResult is ResultVaildator.Invalid) {
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
