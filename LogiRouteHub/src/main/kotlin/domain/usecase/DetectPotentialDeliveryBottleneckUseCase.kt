package domain.usecase

import domain.model.Warehouse
import domain.model.request.PotentialDeliveryBottleneck
import domain.model.request.WarehouseCapacityInfo
import domain.repository.WarehouseRepository


class DetectPotentialDeliveryBottleneckUseCase(
    private val warehouseRepository: WarehouseRepository
) {
    suspend operator fun invoke(): Result<List<PotentialDeliveryBottleneck>> =
        runCatching {
            val warehouses = warehouseRepository.findAll().getOrThrow()

            warehouses.map { warehouse ->
                WarehouseCapacityInfo(
                    warehouse = warehouse,
                    totalCargoWeightKg = warehouse.cargoQueue
                        .sumOf { it.weightByKg },
                    totalVehicleCapacityKg = warehouse.stationedVehicles
                        .sumOf { it.maxCapacityKg }
                )
            }
                .filter { capacityInfo ->
                    capacityInfo.totalCargoWeightKg >
                            capacityInfo.totalVehicleCapacityKg
                }
                .map { capacityInfo ->
                    PotentialDeliveryBottleneck(
                        warehouse = capacityInfo.warehouse,
                        totalCargoWeightKg = capacityInfo.totalCargoWeightKg,
                        totalVehicleCapacityKg = capacityInfo.totalVehicleCapacityKg,
                        capacityGapKg = capacityInfo.totalCargoWeightKg - capacityInfo.totalVehicleCapacityKg
                    )
                }
        }
}
