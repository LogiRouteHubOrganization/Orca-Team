package data.repository.remote

import data.remote.datasource.VehicleRemoteDataSource
import data.remote.exception.toDomainException
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

    override suspend fun findById(vehicleId: String): Vehicle? {
        return try {
            val vehicleDto = vehicleRemoteDataSource.getVehicleById(vehicleId)
                ?: return null

            val currentWarehouse = warehouseRepository.findById(vehicleDto.currentWarehouseId)
                ?: throw ResourceNotFoundException("Warehouse", vehicleDto.currentWarehouseId)

            vehicleDto.toDomain(
                currentWarehouse = currentWarehouse
            )
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Vehicle", resourceId = vehicleId)
        }
    }

    override suspend fun findAll(): List<Vehicle> {
        return try {
            val vehicleDtos = vehicleRemoteDataSource.getAllVehicles()
            val warehouses = warehouseRepository.findAll()
            val warehousesById = warehouses.associateBy { it.id }

            vehicleDtos.map { vehicleDto ->
                val currentWarehouse = warehousesById[vehicleDto.currentWarehouseId]
                    ?: throw ResourceNotFoundException("Warehouse", vehicleDto.currentWarehouseId)

                vehicleDto.toDomain(
                    currentWarehouse = currentWarehouse
                )
            }
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Vehicle")
        }
    }

    override suspend fun add(vehicle: Vehicle): Vehicle {
        return try {
            val createdDto = vehicleRemoteDataSource.createVehicle(
                vehicle.toCreateDto()
            )

            val currentWarehouse = warehouseRepository.findById(createdDto.currentWarehouseId)
                ?: throw ResourceNotFoundException("Warehouse", createdDto.currentWarehouseId)

            createdDto.toDomain(
                currentWarehouse = currentWarehouse
            )
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Vehicle", resourceId = vehicle.id)
        }
    }

    override suspend fun update(
        vehicleId: String,
        updateModel: VehicleUpdateModel
    ): Vehicle {
        return try {
            val updatedDto = vehicleRemoteDataSource.updateVehicle(
                id = vehicleId,
                vehicleRequest = updateModel.toUpdateDto()
            )

            val currentWarehouse = warehouseRepository.findById(updatedDto.currentWarehouseId)
                ?: throw ResourceNotFoundException("Warehouse", updatedDto.currentWarehouseId)

            updatedDto.toDomain(
                currentWarehouse = currentWarehouse
            )
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Vehicle", resourceId = vehicleId)
        }
    }

    override suspend fun delete(vehicleId: String) {
        try {
            vehicleRemoteDataSource.deleteVehicle(vehicleId)
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Vehicle", resourceId = vehicleId)
        }
    }
}
