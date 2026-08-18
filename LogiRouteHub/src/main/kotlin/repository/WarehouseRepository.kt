package repository

import dataholder.WarehouseRaw

interface WarehouseRepository {

    fun loadWarehouses(): List<WarehouseRaw>
}
