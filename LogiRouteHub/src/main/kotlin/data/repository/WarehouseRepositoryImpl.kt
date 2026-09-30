package data.repository

import data.datasource.WarehouseDataSource
import data.exception.local.LocalDataException
import data.exception.local.toDomainException
import data.exception.remote.toDomainException
import data.mapper.WarehouseMapper
import data.remote.datasource.WarehouseRemoteDataSource
import data.remote.mapper.toCreateDto
import data.remote.mapper.toRaw
import data.remote.mapper.toUpdateDto
import domain.exception.ResourceNotFoundException
import domain.model.Warehouse
import domain.model.updateinput.WarehouseUpdateModel
import domain.repository.WarehouseRepository

class WarehouseRepositoryImpl(
    private val warehouseDataSource: WarehouseDataSource,
    private val warehouseRemoteDataSource: WarehouseRemoteDataSource,
    private val warehouseMapper: WarehouseMapper
) : WarehouseRepository {

    override suspend fun findAll(): Result<List<Warehouse>> =
        runCatching {
            loadLocalWarehouses()
        }.mapLocalDataException()

    override suspend fun findById(
        warehouseId: String
    ): Result<Warehouse?> =
        runCatching {
            loadLocalWarehouses().find {
                it.id == warehouseId
            }
        }.mapLocalDataException()

    override suspend fun add(
        warehouse: Warehouse
    ): Result<Warehouse> = runCatching {
            val createdDto = warehouseRemoteDataSource.createWarehouse(
                warehouse.toCreateDto()
            )

            warehouseDataSource.addWarehouse(
                createdDto.toRaw()
            )

            findLocalWarehouseOrThrow(createdDto.id)
        }.mapRepositoryException(
            resourceId = warehouse.id
        )

    override suspend fun update(
        warehouseId: String,
        updateModel: WarehouseUpdateModel
    ): Result<Warehouse> = runCatching {
            val updatedDto = warehouseRemoteDataSource.updateWarehouse(
                id = warehouseId,
                warehouseRequest = updateModel.toUpdateDto()
            )

            warehouseDataSource.updateWarehouse(
                updatedDto.toRaw()
            )

            findLocalWarehouseOrThrow(warehouseId)
        }.mapRepositoryException(
            resourceId = warehouseId
        )

    override suspend fun delete(
        warehouseId: String
    ): Result<Unit> = runCatching {
            warehouseRemoteDataSource.deleteWarehouse(
                warehouseId
            )

            warehouseDataSource.deleteWarehouse(
                warehouseId
            )
        }.mapRepositoryException(
            resourceId = warehouseId
        )

    override suspend fun refresh(): Result<Unit> =
        runCatching {
            val remoteWarehouses = warehouseRemoteDataSource.getAllWarehouses()

            val localWarehouses = remoteWarehouses.map { warehouseDto ->
                    warehouseDto.toRaw()
                }

            warehouseDataSource.replaceWarehouses(
                localWarehouses
            )
        }.mapRepositoryException()

    private fun loadLocalWarehouses(): List<Warehouse> {
        return warehouseDataSource
            .loadWarehouses()
            .map(warehouseMapper::mapToDomainModel)
    }

    private fun findLocalWarehouseOrThrow(
        warehouseId: String
    ): Warehouse {
        return loadLocalWarehouses().find {
            it.id == warehouseId
        } ?: throw ResourceNotFoundException.WarehouseNotFound(
            warehouseId
        )
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
        resourceId: String? = null
    ): Result<T> {
        return recoverCatching { throwable ->
            throw when (throwable) {
                is LocalDataException -> throwable.toDomainException()

                else -> {
                    if (resourceId != null) {
                        throwable.toDomainException(
                            resourceType = "Warehouse",
                            resourceId = resourceId
                        )
                    } else {
                        throwable.toDomainException(
                            resourceType = "Warehouse"
                        )
                    }
                }
            }
        }
    }
}
