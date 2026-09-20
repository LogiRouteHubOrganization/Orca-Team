package domain.usecase

import domain.model.Vehicle

class RecommendVehicleByCapacityAndCostUseCase(
    private val findStationedVehiclesByCapacityUseCase:
    FindStationedVehiclesByCapacityUseCase
) {

    suspend operator fun invoke(
        warehouseId: String,
        requiredLoadKg: Double
    ): Result<Vehicle?> = runCatching {

        val suitableVehicles = findStationedVehiclesByCapacityUseCase(
            warehouseId = warehouseId,
            requiredLoadKg = requiredLoadKg
        ).getOrThrow()

        suitableVehicles.minWithOrNull(
            compareBy<Vehicle> { vehicle ->
                calculateRemainingCapacityAfterLoad(
                    vehicle = vehicle,
                    requiredLoadKg = requiredLoadKg
                )
            }.thenBy { vehicle -> vehicle.costPerKm }
        )
    }
    private fun calculateRemainingCapacityAfterLoad(
        vehicle: Vehicle,
        requiredLoadKg: Double
    ): Double {

        val currentLoadedWeightKg = vehicle.calculateTotalLoadedWeightKg()

        val projectedLoadedWeightKg =
            currentLoadedWeightKg + requiredLoadKg

        return vehicle.maxCapacityKg - projectedLoadedWeightKg
    }
}
