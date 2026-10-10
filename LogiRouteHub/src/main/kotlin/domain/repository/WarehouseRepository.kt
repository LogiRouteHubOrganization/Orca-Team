package domain.repository

import domain.model.Warehouse
import domain.model.updateinput.WarehouseUpdateModel

interface WarehouseRepository {

    suspend fun findById(
        warehouseId: String
    ): Result<Warehouse?>

    suspend fun findAll(): Result<List<Warehouse>>

    suspend fun add(
        warehouse: Warehouse
    ): Result<Warehouse>

    suspend fun update(
        warehouseId: String,
        updateModel: WarehouseUpdateModel
    ): Result<Warehouse>

    suspend fun delete(
        warehouseId: String
    ): Result<Unit>

    suspend fun refresh(): Result<Unit>
}
