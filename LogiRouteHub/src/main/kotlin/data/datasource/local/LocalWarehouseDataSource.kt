
package data.datasource.local

import data.dataholder.WarehouseRaw

interface LocalWarehouseDataSource {

    suspend fun loadWarehouses(): List<WarehouseRaw>

    suspend fun addWarehouse(warehouse: WarehouseRaw)

    suspend fun updateWarehouse(warehouse: WarehouseRaw)

    suspend fun deleteWarehouse(warehouseId: String)

    suspend fun replaceWarehouses(warehouses: List<WarehouseRaw>)
}
