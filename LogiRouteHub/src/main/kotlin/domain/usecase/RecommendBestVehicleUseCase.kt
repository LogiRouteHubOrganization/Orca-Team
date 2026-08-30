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
        val suitableVehicles =
            findStationedVehiclesByCapacityUseCase(
                warehouse = warehouse,
                requiredCapacityKg = requiredCapacityKg
            )

        return suitableVehicles.minWithOrNull(
            compareBy<Vehicle> { vehicle ->
                val currentLoadedWeightKg = vehicle.calculateTotalLoadedWeightKg()

                val projectedLoadedWeightKg = currentLoadedWeightKg + requiredCapacityKg

                vehicle.maxCapacityKg - projectedLoadedWeightKg
            }.thenBy { vehicle ->
                vehicle.costPerKm
            }
        )
    }
}
