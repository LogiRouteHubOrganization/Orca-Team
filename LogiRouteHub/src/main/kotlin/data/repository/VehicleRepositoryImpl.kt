package data.repository

import data.datasource.local.LocalVehicleDataSource
import data.exception.local.LocalDataException
import data.exception.local.toDomainException
import data.exception.remote.toDomainException
import data.mapper.VehicleMapper
import data.datasource.remote.RemoteVehicleDataSource
import data.remote.mapper.toCreateDto
import data.remote.mapper.toRaw
import data.remote.mapper.toUpdateDto
import domain.exception.ResourceNotFoundException
import domain.model.Vehicle
import domain.model.updateinput.VehicleUpdateModel
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository

class VehicleRepositoryImpl(
    private val vehicleDataSource: LocalVehicleDataSource,
    private val vehicleRemoteDataSource: RemoteVehicleDataSource,
    private val vehicleMapper: VehicleMapper,
    private val warehouseRepository: WarehouseRepository
) : VehicleRepository {

    private val vehicles = mutableListOf<Vehicle>()
    private var isLoaded = false

    override suspend fun findAll(): Result<List<Vehicle>> =
        runCatching {
            loadLocalVehicles()
            vehicles.toList()
        }.mapLocalDataException()

    override suspend fun findById(
        vehicleId: String
    ): Result<Vehicle?> = runCatching {
        loadLocalVehicles()
        vehicles.find { vehicle ->
            vehicle.id == vehicleId
        }
    }.mapLocalDataException()

    override suspend fun add(
        vehicle: Vehicle
    ): Result<Vehicle> = runCatching {
        loadLocalVehicles()

        val createdDto = vehicleRemoteDataSource.createVehicle(
            vehicle.toCreateDto()
        )

        val createdRaw = createdDto.toRaw()

        vehicleDataSource.addVehicle(createdRaw)

        val currentWarehouse = warehouseRepository
            .findById(createdRaw.currentHubId)
            .getOrThrow()
            ?: throw ResourceNotFoundException.WarehouseNotFound(
                createdRaw.currentHubId
            )

        val createdVehicle = vehicleMapper.mapToDomainModel(
            rawVehicle = createdRaw,
            currentWarehouse = currentWarehouse
        )

        vehicles.add(createdVehicle)
        currentWarehouse.addVehicle(createdVehicle)

        createdVehicle
    }.mapRepositoryException(
        resourceId = vehicle.id
    )

    override suspend fun update(
        vehicleId: String,
        updateModel: VehicleUpdateModel
    ): Result<Vehicle> = runCatching {
        loadLocalVehicles()

        val previousVehicle = vehicles.find { it.id == vehicleId }
            ?: throw ResourceNotFoundException.VehicleNotFound(vehicleId)

        val updatedRaw = vehicleRemoteDataSource.updateVehicle(
            id = vehicleId,
            vehicleRequest = updateModel.toUpdateDto()
        ).toRaw()

        vehicleDataSource.updateVehicle(updatedRaw)

        val newWarehouse = warehouseRepository.findById(updatedRaw.currentHubId).getOrThrow()
            ?: throw ResourceNotFoundException.WarehouseNotFound(updatedRaw.currentHubId)

        val updatedVehicle = vehicleMapper.mapToDomainModel(updatedRaw, newWarehouse).apply {
            restoreAssignedPackages(previousVehicle.assignedPackages)
        }

        updateVehicleRelations(previousVehicle = previousVehicle, updatedVehicle = updatedVehicle)

        vehicles.replaceAll { if (it.id == vehicleId) updatedVehicle else it }
        updatedVehicle
    }.mapRepositoryException(resourceId = vehicleId)

    override suspend fun delete(
        vehicleId: String
    ): Result<Unit> =
        runCatching {
            loadLocalVehicles()

            val vehicle = vehicles.find { currentVehicle ->
                currentVehicle.id == vehicleId
            } ?: throw ResourceNotFoundException.VehicleNotFound(
                vehicleId
            )

            vehicleRemoteDataSource.deleteVehicle(vehicleId)

            vehicleDataSource.deleteVehicle(vehicleId)

            vehicle.currentWarehouse.removeVehicle(vehicle)
            vehicles.remove(vehicle)

            Unit
        }.mapRepositoryException(
            resourceId = vehicleId
        )

    override suspend fun refresh(): Result<Unit> =
        runCatching {
            val remoteVehicles = vehicleRemoteDataSource.getAllVehicles()

            val localVehicles = remoteVehicles.map { vehicleDto ->
                vehicleDto.toRaw()
            }

            vehicleDataSource.replaceVehicles(
                localVehicles
            )
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Vehicle"
            )
        }

    override suspend fun initializeStationedVehicles(): Result<Unit> = runCatching {
        loadLocalVehicles()
    }.mapLocalDataException()

    private suspend fun loadLocalVehicles() {
        if (isLoaded) {
            return
        }

        val rawVehicles = vehicleDataSource.loadVehicles()

        rawVehicles.forEach { rawVehicle ->

            val currentWarehouse = warehouseRepository
                .findById(rawVehicle.currentHubId)
                .getOrThrow()

            if (currentWarehouse == null) {
                println(
                    "Warning: Skipping vehicle '${rawVehicle.vehicleId}' " +
                            "because warehouse '${rawVehicle.currentHubId}' " +
                            "was not found"
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

    private fun updateVehicleRelations(
        previousVehicle: Vehicle,
        updatedVehicle: Vehicle
    ) {
        val previousWarehouse = previousVehicle.currentWarehouse

        val newWarehouse = updatedVehicle.currentWarehouse

        if (previousWarehouse.id == newWarehouse.id) {

            val vehicleIndex = previousWarehouse.stationedVehicles
                .indexOfFirst { vehicle ->
                    vehicle.id == previousVehicle.id
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

    private fun <T> Result<T>.mapLocalDataException(): Result<T> {
        return recoverCatching { throwable ->
            throw when (throwable) {
                is LocalDataException ->
                    throwable.toDomainException()

                else -> throwable
            }
        }
    }

    private fun <T> Result<T>.mapRepositoryException(
        resourceId: String
    ): Result<T> {
        return recoverCatching { exception ->
            throw when (exception) {
                is LocalDataException ->
                    exception.toDomainException()

                else ->
                    exception.toDomainException(
                        resourceType = "Vehicle",
                        resourceId = resourceId
                    )
            }
        }
    }
}
