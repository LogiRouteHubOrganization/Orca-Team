package domain.builder

import dataHolder.FleetRaw
import dataHolder.LogisticsPackageRaw
import dataHolder.RouteRaw
import dataHolder.WarehouseRaw

data class GraphBuilderInput(
    val warehouses: List<WarehouseRaw>,
    val packages: List<LogisticsPackageRaw>,
    val vehicles: List<FleetRaw>,
    val routes: List<RouteRaw>
)