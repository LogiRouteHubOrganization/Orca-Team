package domain.usecase

import domain.model.Vehicle
import domain.model.Warehouse

class FindStationedVehiclesByCapacityUseCase {

    operator fun invoke(
        warehouse: Warehouse,
        requiredLoadKg: Double
    ): List<Vehicle> {

        return warehouse.stationedVehicles.filter { vehicle ->

            val currentLoadedWeightKg = vehicle.calculateTotalLoadedWeightKg()

            val availableCapacityKg = vehicle.maxCapacityKg - currentLoadedWeightKg
            availableCapacityKg >= requiredLoadKg
        }
    }
}
