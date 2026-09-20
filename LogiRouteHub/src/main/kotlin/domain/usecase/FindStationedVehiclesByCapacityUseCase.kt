package domain.usecase

import domain.model.Vehicle
import domain.repository.WarehouseRepository

class FindStationedVehiclesByCapacityUseCase(
    private val warehouseRepository: WarehouseRepository
) {

    suspend operator fun invoke(
        warehouseId: String,
        requiredLoadKg: Double
    ): Result<List<Vehicle>> = runCatching {

        val warehouse = warehouseRepository.findById(warehouseId).getOrThrow()
            ?: return@runCatching emptyList()

        warehouse.stationedVehicles.filter { vehicle ->
            val currentLoadedWeightKg = vehicle.calculateTotalLoadedWeightKg()
            val availableCapacityKg =
                vehicle.maxCapacityKg - currentLoadedWeightKg

            availableCapacityKg >= requiredLoadKg
        }
    }
}
