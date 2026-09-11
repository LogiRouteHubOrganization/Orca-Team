package data.repository

import data.datasource.VehicleDataSource
import data.mapper.VehicleMapper
import domain.model.Vehicle
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository

class VehicleRepositoryImpl(
    private val vehicleDataSource: VehicleDataSource,
    private val vehicleMapper: VehicleMapper,
    private val warehouseRepository: WarehouseRepository
) : VehicleRepository {

    companion object {
        private const val NOT_FOUND_INDEX = -1
    }

    private val vehicles = mutableListOf<Vehicle>()
    private var isLoaded = false

    private fun loadVehiclesIfNeeded() {
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

    override fun findAll(): List<Vehicle> {
        loadVehiclesIfNeeded()
        return vehicles.toList()
    }

    override fun findById(vehicleId: String): Vehicle? {
        loadVehiclesIfNeeded()
        return vehicles.find {
            it.id == vehicleId
        }
    }

    override fun add(vehicle: Vehicle) {
        loadVehiclesIfNeeded()

        vehicles.add(vehicle)
        vehicle.currentWarehouse.addVehicle(vehicle)
    }

    override fun update(vehicle: Vehicle) {
        loadVehiclesIfNeeded()
        val vehicleIndex = vehicles.indexOfFirst {
            it.id == vehicle.id
        }
        if (vehicleIndex == NOT_FOUND_INDEX) return

        val previousWarehouse = warehouseRepository.findAll()
            .find { warehouse ->
                warehouse.stationedVehicles.any { it.id == vehicle.id }
            } ?: return

        val newWarehouse = vehicle.currentWarehouse

        if (previousWarehouse.id == newWarehouse.id) {
            previousWarehouse.stationedVehicles
                .indexOfFirst { it.id == vehicle.id }
                .takeIf { it != NOT_FOUND_INDEX }
                ?.let { vehicleIndex ->
                    previousWarehouse.updateVehicleAtIndex(
                        vehicleIndex,
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
}
