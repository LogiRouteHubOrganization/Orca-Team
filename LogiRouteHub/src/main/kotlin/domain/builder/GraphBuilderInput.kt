package domain.builder

import data.repository.csv.CsvWarehouseRepository
import data.repository.csv.CsvPackageRepository
import data.repository.csv.CsvVehicleRepository
import data.repository.csv.CsvRouteRepository

data class GraphBuilderInput(
    val packageRepository: CsvPackageRepository,
    val warehouseRepository: CsvWarehouseRepository,
    val vehicleRepository: CsvVehicleRepository,
    val routeRepository: CsvRouteRepository
)

