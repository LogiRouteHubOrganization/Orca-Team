package data.repository.local

import data.datasource.WarehouseDataSource
import data.mapper.WarehouseMapper
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

    override suspend fun findAll(): List<Warehouse> {
        loadWarehousesIfNeeded()
        return warehouses.toList()
    }

    override suspend fun findById(warehouseId: String): Warehouse? {
        loadWarehousesIfNeeded()
        return warehouses.find { it.id == warehouseId }
    }

    override suspend fun add(warehouse: Warehouse): Warehouse {
        loadWarehousesIfNeeded()

        warehouses.add(warehouse)

        return warehouse
    }

    override suspend fun update(
        warehouseId: String,
        updateModel: WarehouseUpdateModel
    ): Warehouse {
        loadWarehousesIfNeeded()

        val warehouse = warehouses.find { it.id == warehouseId }
            ?: throw IllegalStateException("Warehouse not found: $warehouseId")

        warehouse.updateDetails(
            name = updateModel.name ?: warehouse.name,
            regionalZone = updateModel.regionalZone ?: warehouse.regionalZone,
            latitude = updateModel.latitude ?: warehouse.latitude,
            longitude = updateModel.longitude ?: warehouse.longitude
        )

        return warehouse
    }

    override suspend fun delete(warehouseId: String) {
        loadWarehousesIfNeeded()

        warehouses.removeIf { it.id == warehouseId }
    }
}