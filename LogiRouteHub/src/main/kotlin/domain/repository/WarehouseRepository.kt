package domain.repository

import domain.model.Warehouse

interface WarehouseRepository {

    fun findById(warehouseId: String): Warehouse?

    fun findAll(): List<Warehouse>

    fun add(warehouse: Warehouse)

    fun update(warehouse: Warehouse)
}
