package data.repository

import data.dataholder.WarehouseRaw

interface WarehouseRepository {

    fun loadWarehouses(): List<WarehouseRaw>
}
