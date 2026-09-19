package data.remote.datasource

import data.remote.dto.response.WarehouseDto
import data.remote.dto.request.WarehouseCreateRequestDto
import data.remote.dto.request.WarehouseUpdateRequestDto

interface WarehouseRemoteDataSource {

    suspend fun getAllWarehouses(): List<WarehouseDto>

    suspend fun getWarehouseById(id: String): WarehouseDto?

    suspend fun createWarehouse(
        warehouseRequest: WarehouseCreateRequestDto
    ): WarehouseDto

    suspend fun updateWarehouse(
        id: String,
        warehouseRequest: WarehouseUpdateRequestDto
    ): WarehouseDto

    suspend fun deleteWarehouse(id: String)
}
