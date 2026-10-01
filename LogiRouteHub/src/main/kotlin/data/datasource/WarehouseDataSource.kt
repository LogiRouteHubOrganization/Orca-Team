package data.datasource

import data.dataholder.WarehouseRaw

interface WarehouseDataSource {

    fun loadWarehouses(): List<WarehouseRaw>

    fun addWarehouse(warehouse: WarehouseRaw)

    fun updateWarehouse(warehouse: WarehouseRaw)

    fun deleteWarehouse(warehouseId: String)

    fun replaceWarehouses(warehouses: List<WarehouseRaw>)
}
