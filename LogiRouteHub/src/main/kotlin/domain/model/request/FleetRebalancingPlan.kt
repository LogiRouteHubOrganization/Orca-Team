package domain.model.request

import domain.model.Vehicle
import domain.model.Warehouse

data class FleetRebalancingPlan(
    val sourceWarehouse: Warehouse,
    val overloadedWarehouse: Warehouse,
    val vehiclesToMove: List<Vehicle>
)
