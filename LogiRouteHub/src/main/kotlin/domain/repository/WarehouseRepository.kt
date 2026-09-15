package domain.repository

import domain.model.Warehouse
import domain.model.updateinput.WarehouseUpdateModel

interface WarehouseRepository {

    suspend fun findById(warehouseId: String): Warehouse?

    suspend fun findAll(): List<Warehouse>

    suspend fun add(warehouse: Warehouse):Warehouse

    suspend fun update( warehouseId: String, updateModel: WarehouseUpdateModel): Warehouse


    suspend fun delete(warehouseId: String)
}
