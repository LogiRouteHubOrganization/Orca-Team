package domain.usecase

import domain.model.Package
import domain.model.request.DispatchVehicleRequest
import domain.model.updateinput.VehicleUpdateModel
import domain.model.updateinput.WarehouseUpdateModel
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository

class DispatchVehicleUseCase(
    private val vehicleRepository: VehicleRepository,
    private val warehouseRepository: WarehouseRepository
) {

    suspend operator fun invoke(input: DispatchVehicleRequest): List<Package> {

        val vehicle = vehicleRepository.findById(input.vehicleId)
            ?: return emptyList()

        val warehouse = warehouseRepository.findById(input.warehouseId)
            ?: return emptyList()

        val availableCapacity =
            vehicle.maxCapacityKg -
                    vehicle.calculateTotalLoadedWeightKg()

        val packagesToDispatch = warehouse.cargoQueue.fold(
            emptyList<Package>() to availableCapacity
        ) { (selectedPackages, remainingCapacity), logisticsPackage ->

            if (logisticsPackage.weightByKg <= remainingCapacity) {

                (selectedPackages + logisticsPackage) to
                        (remainingCapacity - logisticsPackage.weightByKg)

            } else {
                selectedPackages to remainingCapacity
            }

        }.first

        packagesToDispatch.forEach { logisticsPackage ->
            vehicle.addPackage(logisticsPackage)
            warehouse.removePackage(logisticsPackage)
        }

        val vehicleUpdateModel = VehicleUpdateModel(
            currentWarehouseId = vehicle.currentWarehouse.id
        )
        val warehouseUpdateModel = WarehouseUpdateModel()

        vehicleRepository.update(vehicle.id, vehicleUpdateModel)
        warehouseRepository.update(warehouse.id, warehouseUpdateModel)

        return packagesToDispatch
    }
}
