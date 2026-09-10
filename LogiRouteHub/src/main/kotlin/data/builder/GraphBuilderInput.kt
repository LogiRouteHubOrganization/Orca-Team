package data.builder

import data.datasource.PackageDataSource
import data.datasource.WarehouseDataSource
import data.datasource.RouteDataSource
import data.datasource.VehicleDataSource

data class GraphBuilderInput(
    val packageDataSource: PackageDataSource,
    val warehouseDataSource: WarehouseDataSource,
    val vehicleDataSource: VehicleDataSource,
    val routeDataSource: RouteDataSource
)

