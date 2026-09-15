package data.repository.remote

import data.remote.datasource.VehicleRemoteDataSource
import data.remote.mapper.toCreateDto
import data.remote.mapper.toDomain
import data.remote.mapper.toUpdateDto
import domain.model.Vehicle
import domain.model.updateinput.VehicleUpdateModel
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository

class RemoteVehicleRepositoryImpl(
    private val vehicleRemoteDataSource: VehicleRemoteDataSource, private val warehouseRepository: WarehouseRepository
) : VehicleRepository {

    override suspend fun findById(vehicleId: String): Vehicle? {
        val vehicleDto = vehicleRemoteDataSource.getVehicleById(vehicleId) ?: return null

        val currentWarehouse =
            warehouseRepository.findById(vehicleDto.currentWarehouseId) ?: throw IllegalStateException(
                "Warehouse not found: ${vehicleDto.currentWarehouseId}"
            )

        return vehicleDto.toDomain(
            currentWarehouse = currentWarehouse
        )
    }

    override suspend fun findAll(): List<Vehicle> {
        val vehicleDtos = vehicleRemoteDataSource.getAllVehicles()

        val warehouses = warehouseRepository.findAll()

        val warehousesById = warehouses.associateBy { it.id }

        return vehicleDtos.map { vehicleDto ->

            val currentWarehouse = warehousesById[vehicleDto.currentWarehouseId] ?: throw IllegalStateException(
                "Warehouse not found: ${vehicleDto.currentWarehouseId}"
            )

            vehicleDto.toDomain(
                currentWarehouse = currentWarehouse
            )
        }
    }

    override suspend fun add(vehicle: Vehicle): Vehicle {
        val createdDto = vehicleRemoteDataSource.createVehicle(
            vehicle.toCreateDto()
        )

        val currentWarehouse =
            warehouseRepository.findById(createdDto.currentWarehouseId) ?: throw IllegalStateException(
                "Warehouse not found: ${createdDto.currentWarehouseId}"
            )

        return createdDto.toDomain(
            currentWarehouse = currentWarehouse
        )
    }

    override suspend fun update(
        vehicleId: String, updateModel: VehicleUpdateModel
    ): Vehicle {
        val updatedDto = vehicleRemoteDataSource.updateVehicle(
            id = vehicleId, vehicleRequest = updateModel.toUpdateDto()
        )

        val currentWarehouse =
            warehouseRepository.findById(updatedDto.currentWarehouseId) ?: throw IllegalStateException(
                "Warehouse not found: ${updatedDto.currentWarehouseId}"
            )

        return updatedDto.toDomain(
            currentWarehouse = currentWarehouse
        )
    }

    override suspend fun delete(vehicleId: String) {
        vehicleRemoteDataSource.deleteVehicle(vehicleId)
    }
}