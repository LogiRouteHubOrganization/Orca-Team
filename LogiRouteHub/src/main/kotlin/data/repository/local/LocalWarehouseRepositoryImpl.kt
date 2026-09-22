package data.repository.local

import data.datasource.WarehouseDataSource
import data.exception.local.LocalDataException
import data.exception.local.toDomainException
import data.mapper.WarehouseMapper
import domain.exception.ResourceNotFoundException
import domain.model.Warehouse
import domain.model.updateinput.WarehouseUpdateModel
import domain.repository.WarehouseRepository

class LocalWarehouseRepositoryImpl(
    private val warehouseDataSource: WarehouseDataSource,
    private val warehouseMapper: WarehouseMapper
) : WarehouseRepository {

    private val warehouses = mutableListOf<Warehouse>()
    private var isLoaded = false

    private fun loadWarehousesIfNeeded() {
        if (isLoaded) return

        val rawWarehouses = warehouseDataSource.loadWarehouses()

        rawWarehouses
            .map(warehouseMapper::mapToDomainModel)
            .also(warehouses::addAll)

        isLoaded = true
    }

    override suspend fun findAll(): Result<List<Warehouse>> =
        runCatching {
            loadWarehousesIfNeeded()

            warehouses.toList()
        }.mapLocalDataException()

    override suspend fun findById(
        warehouseId: String
    ): Result<Warehouse?> =
        runCatching {
            loadWarehousesIfNeeded()

            warehouses.find { it.id == warehouseId }
        }.mapLocalDataException()

    override suspend fun add(
        warehouse: Warehouse
    ): Result<Warehouse> =
        runCatching {
            loadWarehousesIfNeeded()

            warehouses.add(warehouse)

            warehouse
        }.mapLocalDataException()

    override suspend fun update(
        warehouseId: String,
        updateModel: WarehouseUpdateModel
    ): Result<Warehouse> =
        runCatching {
            loadWarehousesIfNeeded()

            val warehouse =
                warehouses.find { it.id == warehouseId }
                    ?: throw ResourceNotFoundException.WarehouseNotFound(warehouseId)

            warehouse.updateDetails(
                name = updateModel.name
                    ?: warehouse.name,

                regionalZone = updateModel.regionalZone
                    ?: warehouse.regionalZone,

                latitude = updateModel.latitude
                    ?: warehouse.latitude,

                longitude = updateModel.longitude
                    ?: warehouse.longitude
            )

            warehouse
        }.mapLocalDataException()

    override suspend fun delete(
        warehouseId: String
    ): Result<Unit> =
        runCatching {
            loadWarehousesIfNeeded()

            val warehouse =
                warehouses.find { it.id == warehouseId }
                    ?: throw ResourceNotFoundException.WarehouseNotFound(warehouseId)

            warehouses.remove(warehouse)
            Unit
        }.mapLocalDataException()


    private fun <T> Result<T>.mapLocalDataException(): Result<T> {
        return recoverCatching { throwable ->
            throw when (throwable) {
                is LocalDataException -> throwable.toDomainException()
                else -> throwable
            }
        }
    }
}
