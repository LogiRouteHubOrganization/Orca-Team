package domain.usecase

import domain.model.Warehouse
import domain.model.Vehicle
import domain.repository.WarehouseRepository
import domain.model.request.FleetRebalancingConfig
import domain.usecasePackage.GetWarehouseLoadFactorUseCase


class TriggerFleetRebalancingUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val getWarehouseLoadFactorUseCase: GetWarehouseLoadFactorUseCase
) {

    operator fun invoke(
        config: FleetRebalancingConfig
    ): List<FleetRebalancingPlan> {

        val warehouses = warehouseRepository.findAll()

        val overloadedWarehouses = warehouses
            .filter { warehouse ->
                getWarehouseLoadFactorUseCase(warehouse) >=
                        config.highLoadFactorThreshold
            }

        val underutilizedWarehouses = warehouses
            .filter { warehouse ->
                getWarehouseLoadFactorUseCase(warehouse) <=
                        config.lowLoadFactorThreshold
            }

        return underutilizedWarehouses.flatMap { sourceWarehouse ->
            overloadedWarehouses
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

data class FleetRebalancingPlan(
    val sourceWarehouse: Warehouse,
    val overloadedWarehouse: Warehouse,
    val vehiclesToMove: List<Vehicle>
)
