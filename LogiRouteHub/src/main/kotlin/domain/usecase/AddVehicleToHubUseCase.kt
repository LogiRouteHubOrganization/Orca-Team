package domain.usecase

import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.repository.VehicleRepository

class AddVehicleToHubUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val vehicleRepository: VehicleRepository
) {

    suspend operator fun invoke(
        warehouseId: String,
        vehicleId: String
    ): Warehouse? {

        val warehouse = warehouseRepository.findById(warehouseId)
            ?: return null

        val vehicle = vehicleRepository.findById(vehicleId)
            ?: return null

        warehouse.addVehicle(vehicle)
        vehicle.currentWarehouse = warehouse

        warehouseRepository.update(warehouse)
        vehicleRepository.update(vehicle)

        return warehouse
    }
}
