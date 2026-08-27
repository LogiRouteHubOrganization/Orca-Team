package domain.usecase

import domain.model.Vehicle
import domain.model.Warehouse

class FindStationedVehiclesByCapacityUseCase {

    operator fun invoke(
        warehouse: Warehouse,
        requiredCapacityKg: Double
    ): List<Vehicle> {
        return warehouse.stationedVehicles.filter { vehicle ->
            vehicle.maxCapacityKg >= requiredCapacityKg
        }
    }
}