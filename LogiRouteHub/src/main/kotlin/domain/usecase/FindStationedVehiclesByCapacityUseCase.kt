package domain.usecase

import domain.model.Vehicle
import domain.repository.WarehouseRepository

class FindStationedVehiclesByCapacityUseCase(
    private val warehouseRepository: WarehouseRepository
) {

    operator fun invoke(
        warehouseId: String,
        requiredLoadKg: Double
    ): List<Vehicle> {

        val warehouse = warehouseRepository.findById(warehouseId)
            ?: return emptyList()


        return warehouse.stationedVehicles.filter { vehicle ->
            val currentLoadedWeightKg = vehicle.calculateTotalLoadedWeightKg()
            val availableCapacityKg = vehicle.maxCapacityKg - currentLoadedWeightKg

            availableCapacityKg >= requiredLoadKg
        }
    }
}
