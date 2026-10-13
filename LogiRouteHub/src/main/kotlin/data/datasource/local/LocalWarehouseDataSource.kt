package data.datasource.local

import data.dataholder.WarehouseRaw

interface LocalWarehouseDataSource {

    fun loadWarehouses(): List<WarehouseRaw>

    fun addWarehouse(warehouse: WarehouseRaw)

    fun updateWarehouse(warehouse: WarehouseRaw)

    fun deleteWarehouse(warehouseId: String)

    fun replaceWarehouses(warehouses: List<WarehouseRaw>)
}
