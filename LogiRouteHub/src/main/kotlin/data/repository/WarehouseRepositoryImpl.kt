package data.repository

import data.datasource.local.LocalWarehouseDataSource
import data.datasource.remote.RemoteWarehouseDataSource
import data.extension.mapLocalDataException
import data.extension.mapRepositoryException
import data.extension.runCatchingCancellable
import data.mapper.WarehouseMapper
import data.remote.mapper.toCreateDto
import data.remote.mapper.toRaw
import data.remote.mapper.toUpdateDto
import domain.exception.ResourceNotFoundException
import domain.model.Warehouse
import domain.model.updateinput.WarehouseUpdateModel
import domain.repository.WarehouseRepository

class WarehouseRepositoryImpl(
    private val warehouseDataSource: LocalWarehouseDataSource,
    private val warehouseRemoteDataSource: RemoteWarehouseDataSource,
    private val warehouseMapper: WarehouseMapper
) : WarehouseRepository {
    private val warehouses = mutableListOf<Warehouse>()
    private var isLoaded = false

    override suspend fun findAll(): Result<List<Warehouse>> =
        runCatchingCancellable {
            loadLocalWarehouses().toList()
        }.mapLocalDataException()

    override suspend fun findById(
        warehouseId: String
    ): Result<Warehouse?> =
        runCatchingCancellable {
            loadLocalWarehouses().find {
                it.id == warehouseId
            }
        }.mapLocalDataException()

    override suspend fun add(
        warehouse: Warehouse
    ): Result<Warehouse> =
        runCatchingCancellable {
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
            resourceType = "Warehouse",
            resourceId = warehouse.id
        )

    override suspend fun update(
        warehouseId: String,
        updateModel: WarehouseUpdateModel
    ): Result<Warehouse> = runCatchingCancellable {
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
        resourceType = "Warehouse",
        resourceId = warehouseId
    )

    override suspend fun delete(
        warehouseId: String
    ): Result<Unit> =
        runCatchingCancellable {
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
            resourceType = "Warehouse",
            resourceId = warehouseId
        )

    override suspend fun refresh(): Result<Unit> =
        runCatchingCancellable {
            val remoteWarehouses = warehouseRemoteDataSource.getAllWarehouses()

            val localWarehouses = remoteWarehouses.map { warehouseDto ->
                warehouseDto.toRaw()
            }

            warehouseDataSource.replaceWarehouses(
                localWarehouses
            )

            reloadLocalWarehouses()
        }.mapRepositoryException(
            resourceType = "Warehouse"
        )

    private suspend fun loadLocalWarehouses(): List<Warehouse> {
        if (!isLoaded) {
            reloadLocalWarehouses()
        }
        return warehouses
    }

    private suspend fun reloadLocalWarehouses() {
        val loadedWarehouses = warehouseDataSource
            .loadWarehouses()
            .map(warehouseMapper::mapToDomainModel)

        warehouses.clear()
        warehouses.addAll(loadedWarehouses)

        isLoaded = true
    }
}
