package data.repository.local

import data.datasource.VehicleDataSource
import data.exception.local.LocalDataException
import data.exception.local.toDomainException
import data.mapper.VehicleMapper
import domain.exception.ResourceNotFoundException
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

            val currentWarehouse = warehouseRepository
                    .findById(rawVehicle.currentHubId)
                    .getOrThrow()

            if (currentWarehouse == null) {
                println(
                    "Warning: Skipping vehicle '${rawVehicle.vehicleId}' " +
                            "because warehouse '${rawVehicle.currentHubId}' was not found"
                )
                return@forEach
            }

            val vehicle = vehicleMapper.mapToDomainModel(
                rawVehicle = rawVehicle,
                currentWarehouse = currentWarehouse
            )

            vehicles.add(vehicle)
            currentWarehouse.addVehicle(vehicle)
        }

        isLoaded = true
    }

    override suspend fun findAll(): Result<List<Vehicle>> =
        runCatching {
            loadVehiclesIfNeeded()

            vehicles.toList()
        }.mapLocalDataException()

    override suspend fun findById(
        vehicleId: String
    ): Result<Vehicle?> =
        runCatching {
            loadVehiclesIfNeeded()

            vehicles.find { it.id == vehicleId }
        }.mapLocalDataException()

    override suspend fun add(
        vehicle: Vehicle
    ): Result<Vehicle> =
        runCatching {
            loadVehiclesIfNeeded()

            vehicles.add(vehicle)
            vehicle.currentWarehouse.addVehicle(vehicle)

            vehicle
        }.mapLocalDataException()

    override suspend fun update(
        vehicleId: String,
        updateModel: VehicleUpdateModel
    ): Result<Vehicle> =
        runCatching {
            loadVehiclesIfNeeded()

            val currentVehicle =
                vehicles.find { it.id == vehicleId }
                    ?: throw ResourceNotFoundException.VehicleNotFound(vehicleId)

            val newWarehouse =
                updateModel.currentWarehouseId
                    ?.let { warehouseId ->
                        warehouseRepository
                            .findById(warehouseId)
                            .getOrThrow()
                            ?: throw ResourceNotFoundException.WarehouseNotFound(warehouseId)
                    }
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

            val vehicleIndex =
                vehicles.indexOfFirst { it.id == vehicleId }

            if (vehicleIndex != -1) {
                vehicles[vehicleIndex] = updatedVehicle
            }

            updatedVehicle
        }.mapLocalDataException()

    private fun updateVehicleRelations(
        previousVehicle: Vehicle,
        updatedVehicle: Vehicle
    ) {
        val previousWarehouse =
            previousVehicle.currentWarehouse

        val newWarehouse =
            updatedVehicle.currentWarehouse

        if (previousWarehouse.id == newWarehouse.id) {

            val vehicleIndex =
                previousWarehouse.stationedVehicles
                    .indexOfFirst {
                        it.id == previousVehicle.id
                    }

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

    override suspend fun delete(
        vehicleId: String
    ): Result<Unit> =
        runCatching {
            loadVehiclesIfNeeded()

            val vehicle =
                vehicles.find { it.id == vehicleId }
                    ?: throw ResourceNotFoundException.VehicleNotFound(vehicleId)

            vehicle.currentWarehouse.removeVehicle(vehicle)
            vehicles.remove(vehicle)
            Unit
        }.mapLocalDataException()


    private fun <T> Result<T>.mapLocalDataException(): Result<T> {
        return recoverCatching { throwable ->
            throw when (throwable) {
                is LocalDataException -> throwable.toDomainException()
                else -> throwable
            }
        }
    }
}
