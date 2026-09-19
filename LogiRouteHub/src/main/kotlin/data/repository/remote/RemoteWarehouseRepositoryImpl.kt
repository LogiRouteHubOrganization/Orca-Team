
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

    override suspend fun findById(
        warehouseId: String
    ): Result<Warehouse?> {

        return runCatching {
            warehouseRemoteDataSource
                .getWarehouseById(warehouseId)
                ?.toDomain()
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Warehouse",
                resourceId = warehouseId
            )
        }
    }

    override suspend fun findAll(): Result<List<Warehouse>> {

        return runCatching {
            warehouseRemoteDataSource
                .getAllWarehouses()
                .map { it.toDomain() }
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Warehouse"
            )
        }
    }

    override suspend fun add(
        warehouse: Warehouse
    ): Result<Warehouse> {

        return runCatching {
            val createdDto = warehouseRemoteDataSource.createWarehouse(
                warehouse.toCreateDto()
            )

            createdDto.toDomain()
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Warehouse",
                resourceId = warehouse.id
            )
        }
    }

    override suspend fun update(
        warehouseId: String,
        updateModel: WarehouseUpdateModel
    ): Result<Warehouse> {

        return runCatching {
            val updatedDto = warehouseRemoteDataSource.updateWarehouse(
                id = warehouseId,
                warehouseRequest = updateModel.toUpdateDto()
            )

            updatedDto.toDomain()
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Warehouse",
                resourceId = warehouseId
            )
        }
    }

    override suspend fun delete(
        warehouseId: String
    ): Result<Unit> {

        return runCatching {
            warehouseRemoteDataSource.deleteWarehouse(warehouseId)
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Warehouse",
                resourceId = warehouseId
            )
        }
    }
}
