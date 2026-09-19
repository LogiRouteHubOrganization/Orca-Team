
package data.repository.remote

import data.remote.datasource.VehicleRemoteDataSource
import data.exception.remote.toDomainException
import data.remote.mapper.toCreateDto
import data.remote.mapper.toDomain
import data.remote.mapper.toUpdateDto
import domain.exception.ResourceNotFoundException
import domain.model.Vehicle
import domain.model.updateinput.VehicleUpdateModel
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository

class RemoteVehicleRepositoryImpl(
    private val vehicleRemoteDataSource: VehicleRemoteDataSource,
    private val warehouseRepository: WarehouseRepository
) : VehicleRepository {

    override suspend fun findById(
        vehicleId: String
    ): Result<Vehicle?> {

        return runCatching {
            val vehicleDto = vehicleRemoteDataSource.getVehicleById(vehicleId)
                ?: return@runCatching null

            val currentWarehouse = warehouseRepository
                .findById(vehicleDto.currentWarehouseId)
                .getOrThrow()
                ?: throw ResourceNotFoundException(
                    "Warehouse",
                    vehicleDto.currentWarehouseId
                )

            vehicleDto.toDomain(
                currentWarehouse = currentWarehouse
            )
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Vehicle",
                resourceId = vehicleId
            )
        }
    }

    override suspend fun findAll(): Result<List<Vehicle>> {

        return runCatching {
            val vehicleDtos = vehicleRemoteDataSource.getAllVehicles()

            val warehouses = warehouseRepository
                .findAll()
                .getOrThrow()

            val warehousesById = warehouses.associateBy { it.id }

            vehicleDtos.map { vehicleDto ->

                val currentWarehouse = warehousesById[
                    vehicleDto.currentWarehouseId
                ] ?: throw ResourceNotFoundException(
                    "Warehouse",
                    vehicleDto.currentWarehouseId
                )

                vehicleDto.toDomain(
                    currentWarehouse = currentWarehouse
                )
            }
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Vehicle"
            )
        }
    }

    override suspend fun add(
        vehicle: Vehicle
    ): Result<Vehicle> {

        return runCatching {
            val createdDto = vehicleRemoteDataSource.createVehicle(
                vehicle.toCreateDto()
            )

            val currentWarehouse = warehouseRepository
                .findById(createdDto.currentWarehouseId)
                .getOrThrow()
                ?: throw ResourceNotFoundException(
                    "Warehouse",
                    createdDto.currentWarehouseId
                )

            createdDto.toDomain(
                currentWarehouse = currentWarehouse
            )
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Vehicle",
                resourceId = vehicle.id
            )
        }
    }

    override suspend fun update(
        vehicleId: String,
        updateModel: VehicleUpdateModel
    ): Result<Vehicle> {

        return runCatching {
            val updatedDto = vehicleRemoteDataSource.updateVehicle(
                id = vehicleId,
                vehicleRequest = updateModel.toUpdateDto()
            )

            val currentWarehouse = warehouseRepository
                .findById(updatedDto.currentWarehouseId)
                .getOrThrow()
                ?: throw ResourceNotFoundException(
                    "Warehouse",
                    updatedDto.currentWarehouseId
                )

            updatedDto.toDomain(
                currentWarehouse = currentWarehouse
            )
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Vehicle",
                resourceId = vehicleId
            )
        }
    }

    override suspend fun delete(
        vehicleId: String
    ): Result<Unit> {

        return runCatching {
            vehicleRemoteDataSource.deleteVehicle(vehicleId)
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Vehicle",
                resourceId = vehicleId
            )
        }
    }
}

