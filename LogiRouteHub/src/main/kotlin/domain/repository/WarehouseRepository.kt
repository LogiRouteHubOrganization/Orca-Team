package domain.repository

import domain.model.Warehouse

interface WarehouseRepository {

    suspend fun findById(warehouseId: String): Warehouse?

    suspend fun findAll(): List<Warehouse>

    suspend fun add(warehouse: Warehouse)

    suspend fun update(warehouse: Warehouse)

    suspend fun delete(warehouseId: String)
}
