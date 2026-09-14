package domain.usecase

import domain.model.Warehouse
import domain.repository.WarehouseRepository

data class PotentialDeliveryBottleneck(
    val warehouse: Warehouse,
    val totalCargoWeightKg: Double,
    val totalVehicleCapacityKg: Double,
    val capacityGapKg: Double
)

class DetectDeliveryBottleneckUseCase(
    private val warehouseRepository: WarehouseRepository
) {

    suspend operator fun invoke(): List<PotentialDeliveryBottleneck> {
        val warehouses = warehouseRepository.findAll()

        return warehouses
            .map { warehouse ->
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
                    capacityGapKg =
                        capacityInfo.totalCargoWeightKg -
                                capacityInfo.totalVehicleCapacityKg
                )
            }
    }

    private data class WarehouseCapacityInfo(
        val warehouse: Warehouse,
        val totalCargoWeightKg: Double,
        val totalVehicleCapacityKg: Double
    )
}