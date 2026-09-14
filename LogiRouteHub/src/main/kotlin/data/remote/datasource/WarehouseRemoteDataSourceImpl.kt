package data.remote.datasource

import data.remote.SupabaseClientProvider
import data.remote.dto.WarehouseDto
import data.remote.dto.request.WarehouseRequestDto
import data.remote.utils.safeApiCall
import io.github.jan.supabase.postgrest.from

class WarehouseRemoteDataSourceImpl(
    private val tableName: String = "warehouses"
) : WarehouseRemoteDataSource {

    private val client = SupabaseClientProvider.client

    override suspend fun getAllWarehouses(): List<WarehouseDto> =
        safeApiCall {
            client
                .from(tableName)
                .select()
                .decodeList<WarehouseDto>()
        }

    override suspend fun getWarehouseById(id: String): WarehouseDto? =
        safeApiCall {
            client
                .from(tableName)
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<WarehouseDto>()
        }

    override suspend fun createWarehouse(
        warehouseRequest: WarehouseRequestDto
    ): WarehouseDto =
        safeApiCall {
            client
                .from(tableName)
                .insert(warehouseRequest) {
                    select()
                }
                .decodeSingle<WarehouseDto>()
        }

    override suspend fun updateWarehouse(
        id: String,
        warehouseRequest: WarehouseRequestDto
    ): WarehouseDto =
        safeApiCall {
            client
                .from(tableName)
                .update(warehouseRequest) {
                    filter {
                        eq("id", id)
                    }
                    select()
                }
                .decodeSingle<WarehouseDto>()
        }

    override suspend fun deleteWarehouse(id: String) {
        safeApiCall {
            client
                .from(tableName)
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
        }
    }
}