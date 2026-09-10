package data.initializer.dependencies

import data.mapper.PackageMapper
import data.mapper.RouteMapper
import data.mapper.VehicleMapper
import data.mapper.WarehouseMapper

data class DomainMappers(
    val warehouseMapper: WarehouseMapper,
    val packageMapper: PackageMapper,
    val vehicleMapper: VehicleMapper,
    val routeMapper: RouteMapper
)
