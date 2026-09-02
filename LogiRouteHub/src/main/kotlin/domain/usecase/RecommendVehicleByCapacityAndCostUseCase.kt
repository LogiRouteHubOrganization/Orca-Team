package domain.usecase

import domain.model.Vehicle
import domain.model.Warehouse

class RecommendVehicleByCapacityAndCostUseCase(
    private val findStationedVehiclesByCapacityUseCase:
    FindStationedVehiclesByCapacityUseCase
) {

    operator fun invoke(
        warehouse: Warehouse,
        requiredLoadKg: Double
    ): Vehicle? {

        val suitableVehicles = findStationedVehiclesByCapacityUseCase(
            warehouse = warehouse,
            requiredLoadKg = requiredLoadKg
        )
        return suitableVehicles.minWithOrNull(
            compareBy<Vehicle> { vehicle ->
                calculateRemainingCapacityAfterLoad(
                    vehicle = vehicle,
                    requiredLoadKg = requiredLoadKg
                )
            }.thenBy { vehicle ->
                vehicle.costPerKm
            }
        )
    }

    private fun calculateRemainingCapacityAfterLoad(
        vehicle: Vehicle,
        requiredLoadKg: Double
    ): Double {

        val currentLoadedWeightKg = vehicle.calculateTotalLoadedWeightKg()

        val projectedLoadedWeightKg = currentLoadedWeightKg + requiredLoadKg

        return vehicle.maxCapacityKg - projectedLoadedWeightKg
    }
}
