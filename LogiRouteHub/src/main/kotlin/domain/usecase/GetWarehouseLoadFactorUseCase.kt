package domain.usecase

import domain.extension.runCatchingCancellable
import domain.repository.WarehouseRepository

class GetWarehouseLoadFactorUseCase(
    private val warehouseRepository: WarehouseRepository
) {
    private val initialValueForTheSum = 0.0
    private val zeroCapacity = 0.0

    suspend operator fun invoke(
        warehouseId: String
    ): Result<Double> = runCatchingCancellable  {

        val warehouse = warehouseRepository.findById(warehouseId).getOrThrow()
            ?: return@runCatchingCancellable zeroCapacity


        val totalPackageWeight = warehouse.cargoQueue.map { packageItem -> packageItem.weightByKg }
            .fold(initialValueForTheSum) { totalWeight, packageWeight ->
                totalWeight + packageWeight
            }

        val totalVehicleCapacity = warehouse.stationedVehicles
            .map { vehicle -> vehicle.maxCapacityKg }
            .fold(initialValueForTheSum) { totalCapacity, vehicleCapacity ->
                totalCapacity + vehicleCapacity
            }

        if (totalVehicleCapacity == zeroCapacity) {
            zeroCapacity
        } else {
            totalPackageWeight / totalVehicleCapacity
        }
    }
}
