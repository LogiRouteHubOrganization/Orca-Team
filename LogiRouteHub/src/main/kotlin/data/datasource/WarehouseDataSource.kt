package data.datasource

import data.dataholder.WarehouseRaw

interface WarehouseDataSource {

    fun loadWarehouses(): List<WarehouseRaw>
}
