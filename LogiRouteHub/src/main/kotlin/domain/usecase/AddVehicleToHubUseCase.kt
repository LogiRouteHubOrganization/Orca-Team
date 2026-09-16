package domain.usecase

import domain.model.Warehouse
import domain.model.updateinput.VehicleUpdateModel
import domain.model.updateinput.WarehouseUpdateModel
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository

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


        val vehicleUpdateModel = VehicleUpdateModel(
            currentWarehouseId = warehouse.id
        )

        val warehouseUpdateModel = WarehouseUpdateModel()

        vehicleRepository.update(vehicle.id, vehicleUpdateModel)
        warehouseRepository.update(warehouse.id, warehouseUpdateModel)

        return warehouseRepository.findById(warehouseId)
    }
}
