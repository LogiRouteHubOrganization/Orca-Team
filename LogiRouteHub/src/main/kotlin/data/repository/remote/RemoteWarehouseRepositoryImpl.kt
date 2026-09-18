package data.repository.remote

import data.remote.datasource.WarehouseRemoteDataSource
import data.remote.exception.toDomainException
import data.remote.mapper.toCreateDto
import data.remote.mapper.toDomain
import data.remote.mapper.toUpdateDto
import domain.model.Warehouse
import domain.model.updateinput.WarehouseUpdateModel
import domain.repository.WarehouseRepository

class RemoteWarehouseRepositoryImpl(
    private val warehouseRemoteDataSource: WarehouseRemoteDataSource
) : WarehouseRepository {

    override suspend fun findById(warehouseId: String): Warehouse? {
        return try {
            warehouseRemoteDataSource
                .getWarehouseById(warehouseId)
                ?.toDomain()
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Warehouse", resourceId = warehouseId)
        }
    }

    override suspend fun findAll(): List<Warehouse> {
        return try {
            warehouseRemoteDataSource
                .getAllWarehouses()
                .map { it.toDomain() }
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Warehouse")
        }
    }

    override suspend fun add(warehouse: Warehouse): Warehouse {
        return try {
            val createdDto = warehouseRemoteDataSource.createWarehouse(
                warehouse.toCreateDto()
            )
            createdDto.toDomain()
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Warehouse", resourceId = warehouse.id)
        }
    }

    override suspend fun update(
        warehouseId: String,
        updateModel: WarehouseUpdateModel
    ): Warehouse {
        return try {
            val updatedDto = warehouseRemoteDataSource.updateWarehouse(
                id = warehouseId,
                warehouseRequest = updateModel.toUpdateDto()
            )
            updatedDto.toDomain()
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Warehouse", resourceId = warehouseId)
        }
    }

    override suspend fun delete(warehouseId: String) {
        try {
            warehouseRemoteDataSource.deleteWarehouse(warehouseId)
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Warehouse", resourceId = warehouseId)
        }
    }
}
