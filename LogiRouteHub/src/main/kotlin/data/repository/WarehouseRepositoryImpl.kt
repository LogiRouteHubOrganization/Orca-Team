package data.repository

import data.datasource.WarehouseDataSource
import data.mapper.WarehouseMapper
import domain.model.Warehouse
import domain.repository.WarehouseRepository

class WarehouseRepositoryImpl(
    private val warehouseDataSource: WarehouseDataSource,
    private val warehouseMapper: WarehouseMapper
) : WarehouseRepository {

    companion object {
        private const val NOT_FOUND_INDEX = -1
    }

    private val warehouses = mutableListOf<Warehouse>()
    private var isLoaded = false

    private suspend fun loadWarehousesIfNeeded() {
        if (isLoaded) return

        val rawWarehouses = warehouseDataSource.loadWarehouses()

        for (rawWarehouse in rawWarehouses) {
            val warehouse = warehouseMapper.mapToDomainModel(rawWarehouse)
            warehouses.add(warehouse)
        }

        isLoaded = true
    }

    override suspend fun findAll(): List<Warehouse> {
        loadWarehousesIfNeeded()
        return warehouses.toList()
    }

    override suspend fun findById(warehouseId: String): Warehouse? {
        loadWarehousesIfNeeded()
        return warehouses.find { it.id == warehouseId }
    }

    override suspend fun add(warehouse: Warehouse) {
        loadWarehousesIfNeeded()
        warehouses.add(warehouse)
    }

    override suspend fun update(warehouse: Warehouse) {
        loadWarehousesIfNeeded()

        val warehouseIndex = warehouses.indexOfFirst {
            it.id == warehouse.id
        }

        if (warehouseIndex != NOT_FOUND_INDEX) {
            warehouses[warehouseIndex] = warehouse
        }
    }

    override suspend fun delete(warehouseId: String) {
        loadWarehousesIfNeeded()

        val warehouseIndex = warehouses.indexOfFirst {
            it.id == warehouseId
        }

        if (warehouseIndex != NOT_FOUND_INDEX) {
            warehouses.removeAt(warehouseIndex)
        }
    }
}