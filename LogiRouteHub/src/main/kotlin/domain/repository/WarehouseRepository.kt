package domain.repository

import domain.model.Warehouse
import domain.model.Package

interface WarehouseRepository {

    fun findById(warehouseId: String): Warehouse?

    fun findAll(): List<Warehouse>

    fun update(warehouse: Warehouse)

    fun addPackage(warehouse: Warehouse, logisticsPackage: Package)
}




