package domain.usecase

import domain.model.Warehouse

data class WarehouseNetworkSummary(
    val warehouseCount: Int,
    val packageCount: Int,
    val vehicleCount: Int,
    val routeCount: Int,
    val totalCargoWeightKg: Double
)
class GetWarehouseNetworkSummaryUseCase {

    operator fun invoke(
        warehouseIndex: Map<String, Warehouse>
    ): WarehouseNetworkSummary {

        val warehouseCount = warehouseIndex.size

        val packageCount = warehouseIndex.values
            .flatMap { it.cargoQueue }
            .size

        val vehicleCount = warehouseIndex.values
            .flatMap { it.stationedVehicles }
            .size

        val routeCount = warehouseIndex.values
            .flatMap { it.outgoingRoutes }
            .size

        val totalCargoWeightKg = warehouseIndex.values
            .flatMap { it.cargoQueue }
            .sumOf { it.weightByKg }

        return WarehouseNetworkSummary(
            warehouseCount = warehouseCount,
            packageCount = packageCount,
            vehicleCount = vehicleCount,
            routeCount = routeCount,
            totalCargoWeightKg = totalCargoWeightKg
        )
    }
}
