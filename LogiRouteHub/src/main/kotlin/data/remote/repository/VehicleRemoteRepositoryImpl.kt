package data.remote.repository

import data.remote.datasource.VehicleRemoteDataSource
import data.remote.mapper.toDomain
import data.remote.mapper.toRequestDto
import data.remote.utils.toDomainException
import domain.model.Vehicle
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository

class VehicleRemoteRepositoryImpl(
    private val remoteDataSource: VehicleRemoteDataSource,
    private val warehouseRepository: WarehouseRepository
) : VehicleRepository {

    override suspend fun findAll(): List<Vehicle> {
        return try {
            remoteDataSource.getAllVehicles().mapNotNull { dto ->
                val currentWarehouse =
                    warehouseRepository.findById(dto.currentWarehouseId)

                if (currentWarehouse == null) {
                    null
                } else {
                    dto.toDomain(
                        currentWarehouse = currentWarehouse
                    )
                }
            }
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }

    override suspend fun findById(
        vehicleId: String
    ): Vehicle? {
        return try {
            val dto = remoteDataSource.getVehicleById(vehicleId)
                ?: return null

            val currentWarehouse =
                warehouseRepository.findById(dto.currentWarehouseId)
                    ?: return null

            dto.toDomain(
                currentWarehouse = currentWarehouse
            )
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }

    override suspend fun add(
        vehicle: Vehicle
    ) {
        try {
            remoteDataSource.createVehicle(
                vehicle.toRequestDto()
            )
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }

    override suspend fun update(
        vehicle: Vehicle
    ) {
        try {
            remoteDataSource.updateVehicle(
                vehicle.id,
                vehicle.toRequestDto()
            )
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }

    override suspend fun delete(
        vehicleId: String
    ) {
        try {
            remoteDataSource.deleteVehicle(vehicleId)
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }
}