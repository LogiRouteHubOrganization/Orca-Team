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
    private val warehouses = mutableListOf<Warehouse>()
    private var isLoaded = false

    override suspend fun findAll(): Result<List<Warehouse>> =
        runCatching {
            loadLocalWarehouses().toList()
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
    ): Result<Warehouse> =
        runCatching {
            loadLocalWarehouses()

            val createdDto = warehouseRemoteDataSource.createWarehouse(
                    warehouse.toCreateDto()
                )

            val createdRaw = createdDto.toRaw()

            warehouseDataSource.addWarehouse(createdRaw)

            val createdWarehouse = warehouseMapper.mapToDomainModel(
                    createdRaw
                )

            warehouses.add(createdWarehouse)

            createdWarehouse
        }.mapRepositoryException(
            resourceId = warehouse.id
        )

    override suspend fun update(
        warehouseId: String,
        updateModel: WarehouseUpdateModel
    ): Result<Warehouse> = runCatching {
            loadLocalWarehouses()
            val updatedDto = warehouseRemoteDataSource.updateWarehouse(
                    id = warehouseId,
                    warehouseRequest = updateModel.toUpdateDto()
                )

            warehouseDataSource.updateWarehouse(
                updatedDto.toRaw()
            )

            val existingWarehouse = warehouses.find {
                    it.id == warehouseId
                } ?: throw ResourceNotFoundException.WarehouseNotFound(
                    warehouseId
                )

            existingWarehouse.updateDetails(
                name = updatedDto.name,
                regionalZone = updatedDto.regionalZone,
                latitude = updatedDto.latitude,
                longitude = updatedDto.longitude
            )

            existingWarehouse
        }.mapRepositoryException(
            resourceId = warehouseId
        )

    override suspend fun delete(
        warehouseId: String
    ): Result<Unit> =
        runCatching {
            loadLocalWarehouses()
            warehouseRemoteDataSource.deleteWarehouse(
                warehouseId
            )
            warehouseDataSource.deleteWarehouse(warehouseId)

            val warehouse = warehouses.find {
                    it.id == warehouseId
                } ?: throw ResourceNotFoundException.WarehouseNotFound(
                    warehouseId
                )

            warehouses.remove(warehouse)

            Unit
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

            reloadLocalWarehouses()
        }.mapRepositoryException()

    private fun loadLocalWarehouses(): List<Warehouse> {
        if (!isLoaded) {
            reloadLocalWarehouses()
        }

        return warehouses
    }

    private fun reloadLocalWarehouses() {
        val loadedWarehouses = warehouseDataSource
                .loadWarehouses()
                .map(warehouseMapper::mapToDomainModel)

        warehouses.clear()
        warehouses.addAll(loadedWarehouses)

        isLoaded = true
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
