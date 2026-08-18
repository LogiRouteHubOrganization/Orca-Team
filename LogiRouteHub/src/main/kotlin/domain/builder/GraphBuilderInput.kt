package domain.builder

import dataholder.FleetRaw
import dataholder.LogisticsPackageRaw
import dataholder.RouteRaw
import dataholder.WarehouseRaw

data class GraphBuilderInput(
    val warehouses: List<WarehouseRaw>,
    val packages: List<LogisticsPackageRaw>,
    val vehicles: List<FleetRaw>,
    val routes: List<RouteRaw>
)
