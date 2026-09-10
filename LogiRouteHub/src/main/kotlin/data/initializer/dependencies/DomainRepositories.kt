package data.initializer.dependencies

import domain.repository.PackageRepository
import domain.repository.RouteRepository
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository

data class DomainRepositories(
    val warehouseRepository: WarehouseRepository,
    val packageRepository: PackageRepository,
    val vehicleRepository: VehicleRepository,
    val routeRepository: RouteRepository
)
