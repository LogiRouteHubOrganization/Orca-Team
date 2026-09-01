package domain.repository

import data.dataholder.LogisticsPackageRaw
import domain.model.Warehouse
import domain.model.Package

interface WarehouseRepository {

    fun findById(warehouseId: String): Warehouse?

    fun update(warehouse: Warehouse)

    fun addPackage(warehouse: Warehouse, logisticsPackage: Package)
}



