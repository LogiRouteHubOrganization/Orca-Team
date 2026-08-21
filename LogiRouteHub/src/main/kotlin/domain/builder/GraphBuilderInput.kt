package domain.builder

import repository.csv.CsvWarehouseRepository
import repository.csv.CsvPackageRepository
import repository.csv.CsvVehicleRepository
import repository.csv.CsvRouteRepository

data class GraphBuilderInput(
    val packageRepository: CsvPackageRepository,
    val warehouseRepository: CsvWarehouseRepository,
    val vehicleRepository: CsvVehicleRepository,
    val routeRepository: CsvRouteRepository
)

