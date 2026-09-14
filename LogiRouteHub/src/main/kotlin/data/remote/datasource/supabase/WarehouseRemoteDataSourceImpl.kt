package data.remote.datasource.supabase

import data.remote.SupabaseClientProvider
import data.remote.datasource.WarehouseRemoteDataSource
import data.remote.dto.WarehouseDto
import data.remote.dto.request.WarehouseRequestDto
import data.remote.utils.safeApiCall
import io.github.jan.supabase.postgrest.from

class WarehouseRemoteDataSourceImpl : WarehouseRemoteDataSource {

    private val client = SupabaseClientProvider.client
    private val tableName = "warehouses"

    override suspend fun getAllWarehouses(): List<WarehouseDto> {
        return safeApiCall {
            client.from(tableName)
                .select()
                .decodeList<WarehouseDto>()
        }
    }

    override suspend fun getWarehouseById(id: String): WarehouseDto? {
        return safeApiCall {
            client.from(tableName)
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<WarehouseDto>()
        }
    }

    override suspend fun createWarehouse(warehouseRequest: WarehouseRequestDto): WarehouseDto {
        return safeApiCall {
            client.from(tableName)
                .insert(warehouseRequest) {
                    select()
                }
                .decodeSingle<WarehouseDto>()
        }
    }

    override suspend fun updateWarehouse(id: String, warehouseRequest: WarehouseRequestDto): WarehouseDto {
        return safeApiCall {
            client.from(tableName)
                .update(warehouseRequest) {
                    filter {
                        eq("id", id)
                    }
                    select()
                }
                .decodeSingle<WarehouseDto>()
        }
    }

    override suspend fun deleteWarehouse(id: String) {
        safeApiCall {
            client.from(tableName)
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
        }
    }
}
