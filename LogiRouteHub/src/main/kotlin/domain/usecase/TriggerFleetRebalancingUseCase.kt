package domain.usecasePackage

import domain.model.Vehicle
import domain.model.Warehouse

class TriggerFleetRebalancingUseCase(
    private val getWarehouseLoadFactorUseCase: GetWarehouseLoadFactorUseCase
) {

    operator fun invoke(
        warehouses: List<Warehouse>,
        highLoadFactorThreshold: Double = 1.5,
        lowLoadFactorThreshold: Double = 0.5,
        maximumVehiclesToRebalance: Int = 1
    ): List<FleetRebalancingPlan> {

        val overloadedWarehouses = warehouses
            .filter { warehouse ->
                getWarehouseLoadFactorUseCase(warehouse) >= highLoadFactorThreshold
            }

        val underutilizedWarehouses = warehouses
            .filter { warehouse ->
                getWarehouseLoadFactorUseCase(warehouse) <= lowLoadFactorThreshold
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
                        maximumVehiclesToRebalance = maximumVehiclesToRebalance
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

