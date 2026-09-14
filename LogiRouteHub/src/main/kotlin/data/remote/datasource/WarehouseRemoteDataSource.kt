package data.remote.datasource

import data.remote.dto.WarehouseDto
import data.remote.dto.request.WarehouseRequestDto

interface WarehouseRemoteDataSource {
    suspend fun getAllWarehouses(): List<WarehouseDto>
    suspend fun getWarehouseById(id: String): WarehouseDto?
    suspend fun createWarehouse(warehouseRequest: WarehouseRequestDto): WarehouseDto
    suspend fun updateWarehouse(id: String, warehouseRequest: WarehouseRequestDto): WarehouseDto
    suspend fun deleteWarehouse(id: String)
}
