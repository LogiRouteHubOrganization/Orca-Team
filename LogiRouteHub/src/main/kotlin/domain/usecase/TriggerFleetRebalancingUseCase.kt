package domain.usecase

import domain.extension.runCatchingCancellable
import domain.model.Vehicle
import domain.model.Warehouse
import domain.model.request.FleetRebalancingConfig
import domain.model.request.FleetRebalancingPlan
import domain.repository.WarehouseRepository

class TriggerFleetRebalancingUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val getWarehouseLoadFactorUseCase: GetWarehouseLoadFactorUseCase
) {

    suspend operator fun invoke(
        config: FleetRebalancingConfig
    ): Result<List<FleetRebalancingPlan>> = runCatchingCancellable {

        val warehouses = warehouseRepository.findAll().getOrThrow()

        val overloadedWarehouses = mutableListOf<Warehouse>()
        val underutilizedWarehouses = mutableListOf<Warehouse>()

        for (warehouse in warehouses) {
            val loadFactor = getWarehouseLoadFactorUseCase(warehouse.id).getOrThrow()

            if (loadFactor >= config.highLoadFactorThreshold) {
                overloadedWarehouses.add(warehouse)
            }

            if (loadFactor <= config.lowLoadFactorThreshold) {
                underutilizedWarehouses.add(warehouse)
            }
        }

        underutilizedWarehouses.flatMap { sourceWarehouse -> overloadedWarehouses
                .filter { targetWarehouse ->
                    targetWarehouse.id != sourceWarehouse.id
                }
                .mapNotNull { targetWarehouse ->
                    buildFleetRebalancingPlan(
                        sourceWarehouse = sourceWarehouse,
                        overloadedWarehouse = targetWarehouse,
                        maximumVehiclesToRebalance =
                            config.maximumVehiclesToRebalance
                    )
                }
        }
    }
    private fun buildFleetRebalancingPlan(
        sourceWarehouse: Warehouse,
        overloadedWarehouse: Warehouse,
        maximumVehiclesToRebalance: Int
    ): FleetRebalancingPlan? {

        val availableEmptyVehicles = sourceWarehouse.stationedVehicles
            .filter { vehicle ->
                vehicle.calculateTotalLoadedWeightKg() == 0.0
            }
            .take(maximumVehiclesToRebalance)

        return if (availableEmptyVehicles.isNotEmpty()) {
            FleetRebalancingPlan(
                sourceWarehouse = sourceWarehouse,
                overloadedWarehouse = overloadedWarehouse,
                vehiclesToMove = availableEmptyVehicles
            )
        } else {
            null
        }
    }
}
