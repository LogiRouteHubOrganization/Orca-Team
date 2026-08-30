package domain.usecase

import domain.model.Vehicle
import domain.model.Warehouse

class RecommendBestVehicleUseCase(
    private val findStationedVehiclesByCapacityUseCase:
    FindStationedVehiclesByCapacityUseCase
) {

    operator fun invoke(
        warehouse: Warehouse,
        requiredCapacityKg: Double
    ): Vehicle? {
        val suitableVehicles = findStationedVehiclesByCapacityUseCase(
            warehouse = warehouse,
            requiredCapacityKg = requiredCapacityKg
        )
        return suitableVehicles.minByOrNull { vehicle ->
            vehicle.costPerKm
        }
    }
}
