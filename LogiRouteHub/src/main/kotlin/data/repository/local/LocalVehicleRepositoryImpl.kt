package data.repository.local

import data.datasource.VehicleDataSource
import data.mapper.VehicleMapper
import domain.model.Vehicle
import domain.model.updateinput.VehicleUpdateModel
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository

class LocalVehicleRepositoryImpl(
    private val vehicleDataSource: VehicleDataSource,
    private val vehicleMapper: VehicleMapper,
    private val warehouseRepository: WarehouseRepository
) : VehicleRepository {

    private val vehicles = mutableListOf<Vehicle>()
    private var isLoaded = false

    private suspend fun loadVehiclesIfNeeded() {
        if (isLoaded) return

        val rawVehicles = vehicleDataSource.loadVehicles()

        rawVehicles.forEach { rawVehicle ->
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

        return vehicles.find { it.id == vehicleId }
    }

    override suspend fun add(vehicle: Vehicle): Vehicle {
        loadVehiclesIfNeeded()

        vehicles.add(vehicle)
        vehicle.currentWarehouse.addVehicle(vehicle)

        return vehicle
    }

    override suspend fun update(
        vehicleId: String,
        updateModel: VehicleUpdateModel
    ): Vehicle {
        loadVehiclesIfNeeded()

        val currentVehicle = vehicles.find { it.id == vehicleId }
            ?: throw IllegalStateException("Vehicle not found: $vehicleId")

        val newWarehouse =
            updateModel.currentWarehouseId
                ?.let { warehouseRepository.findById(it) }
                ?: currentVehicle.currentWarehouse

        val updatedVehicle = currentVehicle.copy(
            maxCapacityKg = updateModel.maxCapacityKg
                ?: currentVehicle.maxCapacityKg,
            costPerKm = updateModel.costPerKm
                ?: currentVehicle.costPerKm,
            currentWarehouse = newWarehouse
        )

        updateVehicleRelations(
            previousVehicle = currentVehicle,
            updatedVehicle = updatedVehicle
        )

        val vehicleIndex = vehicles.indexOfFirst { it.id == vehicleId }
        vehicles[vehicleIndex] = updatedVehicle

        return updatedVehicle
    }

    private fun updateVehicleRelations(
        previousVehicle: Vehicle,
        updatedVehicle: Vehicle
    ) {
        val previousWarehouse = previousVehicle.currentWarehouse
        val newWarehouse = updatedVehicle.currentWarehouse

        if (previousWarehouse.id == newWarehouse.id) {
            val vehicleIndex =
                previousWarehouse.stationedVehicles
                    .indexOfFirst { it.id == previousVehicle.id }

            if (vehicleIndex != -1) {
                previousWarehouse.updateVehicleAtIndex(
                    vehicleIndex,
                    updatedVehicle
                )
            }

            return
        }

        previousWarehouse.removeVehicle(previousVehicle)
        newWarehouse.addVehicle(updatedVehicle)
    }

    override suspend fun delete(vehicleId: String) {
        loadVehiclesIfNeeded()

        val vehicle = vehicles.find { it.id == vehicleId } ?: return

        vehicle.currentWarehouse.removeVehicle(vehicle)
        vehicles.remove(vehicle)
    }
}