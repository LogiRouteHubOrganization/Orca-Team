package domain.builder

import repository.WarehouseRepository
import repository.PackageRepository
import repository.VehicleRepository
import repository.RouteRepository

data class GraphBuilderInput(
    val packageRepository: PackageRepository,
    val warehouseRepository: WarehouseRepository,
    val vehicleRepository: VehicleRepository,
    val routeRepository: RouteRepository
)


