package domain.builder

import data.repository.PackageRepository
import data.repository.WarehouseRepository
import data.repository.RouteRepository
import data.repository.VehicleRepository

data class GraphBuilderInput(
    val packageRepository: PackageRepository,
    val warehouseRepository: WarehouseRepository,
    val vehicleRepository: VehicleRepository,
    val routeRepository: RouteRepository
)

