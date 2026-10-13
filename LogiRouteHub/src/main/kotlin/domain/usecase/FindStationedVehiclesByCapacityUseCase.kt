package domain.usecase

import domain.extension.runCatchingCancellable
import domain.model.Vehicle
import domain.repository.WarehouseRepository

class FindStationedVehiclesByCapacityUseCase(
    private val warehouseRepository: WarehouseRepository
) {

    suspend operator fun invoke(
        warehouseId: String,
        requiredLoadKg: Double
    ): Result<List<Vehicle>> = runCatchingCancellable {

        val warehouse = warehouseRepository.findById(warehouseId).getOrThrow()
            ?: return@runCatchingCancellable emptyList()

        warehouse.stationedVehicles.filter { vehicle ->
            val currentLoadedWeightKg = vehicle.calculateTotalLoadedWeightKg()
            val availableCapacityKg = vehicle.maxCapacityKg - currentLoadedWeightKg

            availableCapacityKg >= requiredLoadKg
        }
    }
}
