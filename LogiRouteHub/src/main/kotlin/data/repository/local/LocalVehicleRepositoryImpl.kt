package data.repository.local

import data.datasource.VehicleDataSource
import data.mapper.VehicleMapper
import domain.model.Vehicle
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository

class LocalVehicleRepositoryImpl(
    private val vehicleDataSource: VehicleDataSource,
    private val vehicleMapper: VehicleMapper,
    private val warehouseRepository: WarehouseRepository
) : VehicleRepository {

    companion object {
        private const val NOT_FOUND_INDEX = -1
    }

    private val vehicles = mutableListOf<Vehicle>()
    private var isLoaded = false

    private suspend fun loadVehiclesIfNeeded() {
        if (isLoaded) return

        val rawVehicles = vehicleDataSource.loadVehicles()

        for (rawVehicle in rawVehicles) {
            val currentWarehouse =
                warehouseRepository.findById(rawVehicle.currentHubId)

            if (currentWarehouse != null) {
                val vehicle = vehicleMapper.mapToDomainModel(
                    rawVehicle = rawVehicle,
                    currentWarehouse = currentWarehouse
                )

                vehicles.add(vehicle)
                currentWarehouse.addVehicle(vehicle)
            }
        }

        isLoaded = true
    }

    override suspend fun findAll(): List<Vehicle> {
        loadVehiclesIfNeeded()
        return vehicles.toList()
    }

    override suspend fun findById(vehicleId: String): Vehicle? {
        loadVehiclesIfNeeded()

        return vehicles.find {
            it.id == vehicleId
        }
    }

    override suspend fun add(vehicle: Vehicle) {
        loadVehiclesIfNeeded()

        vehicles.add(vehicle)
        vehicle.currentWarehouse.addVehicle(vehicle)
    }

    override suspend fun update(vehicle: Vehicle) {
        loadVehiclesIfNeeded()

        val vehicleIndex =
            vehicles.indexOfFirst { it.id == vehicle.id }

        if (vehicleIndex == NOT_FOUND_INDEX) return

        val previousWarehouse = warehouseRepository.findAll()
            .find { warehouse ->
                warehouse.stationedVehicles.any {
                    it.id == vehicle.id
                }
            } ?: return

        val newWarehouse = vehicle.currentWarehouse

        if (previousWarehouse.id == newWarehouse.id) {
            previousWarehouse.stationedVehicles
                .indexOfFirst { it.id == vehicle.id }
                .takeIf { it != NOT_FOUND_INDEX }
                ?.let { warehouseVehicleIndex ->
                    previousWarehouse.updateVehicleAtIndex(
                        warehouseVehicleIndex,
                        vehicle
                    )
                }

            vehicles[vehicleIndex] = vehicle
            return
        }

        previousWarehouse.stationedVehicles
            .find { it.id == vehicle.id }
            ?.let { previousVehicle ->
                previousWarehouse.removeVehicle(previousVehicle)
            }

        newWarehouse.addVehicle(vehicle)
        vehicles[vehicleIndex] = vehicle
    }

    override suspend fun delete(vehicleId: String) {
        loadVehiclesIfNeeded()

        val vehicle =
            vehicles.find { it.id == vehicleId } ?: return

        vehicle.currentWarehouse.removeVehicle(vehicle)
        vehicles.remove(vehicle)
    }
}
