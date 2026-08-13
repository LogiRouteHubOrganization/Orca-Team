package repository

import dataHolder.WarehouseRaw

interface WarehouseRepository {

    fun loadWarehouses(): List<WarehouseRaw>
}