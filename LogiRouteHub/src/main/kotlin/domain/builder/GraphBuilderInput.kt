package domain.builder

import repository.PackageRepository
import repository.RouteRepository
import repository.VehicleRepository
import repository.WarehouseRepository

data class GraphBuilderInput(
    val packageRepository: PackageRepository,
    val warehouseRepository: WarehouseRepository,
    val vehicleRepository: VehicleRepository,
    val routeRepository: RouteRepository
)

