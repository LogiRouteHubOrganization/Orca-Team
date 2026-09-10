package domain.usecase

import domain.model.Vehicle

class RecommendVehicleByCapacityAndCostUseCase(
    private val findStationedVehiclesByCapacityUseCase:
    FindStationedVehiclesByCapacityUseCase
) {

    operator fun invoke(
        warehouseId: String,
        requiredLoadKg: Double
    ): Vehicle? {

        val suitableVehicles = findStationedVehiclesByCapacityUseCase(
            warehouseId = warehouseId,
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
