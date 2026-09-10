package data.initializer.dependencies

import data.datasource.PackageDataSource
import data.datasource.RouteDataSource
import data.datasource.VehicleDataSource
import data.datasource.WarehouseDataSource

data class DomainDataSources(
    val warehouseDataSource: WarehouseDataSource,
    val packageDataSource: PackageDataSource,
    val vehicleDataSource: VehicleDataSource,
    val routeDataSource: RouteDataSource
)
