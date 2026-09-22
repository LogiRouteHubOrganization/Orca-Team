package data.remote.datasource.supabase

import data.remote.SupabaseClientProvider
import data.remote.datasource.WarehouseRemoteDataSource
import data.remote.dto.response.WarehouseDto
import data.remote.dto.request.WarehouseCreateRequestDto
import data.remote.dto.request.WarehouseUpdateRequestDto
import data.remote.utils.safeApiCall
import data.remote.retry.retryWithBackoff
import data.remote.retry.shouldRetry
import io.github.jan.supabase.postgrest.from

class WarehouseRemoteDataSourceImpl : WarehouseRemoteDataSource {

    private val client = SupabaseClientProvider.client
    private val tableName = "warehouses"

    override suspend fun getAllWarehouses(): List<WarehouseDto> {
        val result = retryWithBackoff(
            shouldRetry = { shouldRetry(it) }
        ) {
            safeApiCall {
                client.from(tableName)
                    .select()
                    .decodeList<WarehouseDto>()
            }
        }
        return result.getOrThrow()
    }

    override suspend fun getWarehouseById(
        id: String
    ): WarehouseDto? {
        val result = retryWithBackoff(
            shouldRetry = { shouldRetry(it) }
        ) {
            safeApiCall {
                client.from(tableName)
                    .select {
                        filter {
                            eq("id", id)
                        }
                    }
                    .decodeSingleOrNull<WarehouseDto>()
            }
        }
        return result.getOrThrow()
    }

    override suspend fun createWarehouse(
        warehouseRequest: WarehouseCreateRequestDto
    ): WarehouseDto {
        val result = retryWithBackoff(
            shouldRetry = { shouldRetry(it) }
        ) {
            safeApiCall {
                client.from(tableName)
                    .insert(warehouseRequest) {
                        select()
                    }
                    .decodeSingle<WarehouseDto>()
            }
        }
        return result.getOrThrow()
    }

    override suspend fun updateWarehouse(
        id: String,
        warehouseRequest: WarehouseUpdateRequestDto
    ): WarehouseDto {
        val result = retryWithBackoff(
            shouldRetry = { shouldRetry(it) }
        ) {
            safeApiCall {
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
        return result.getOrThrow()
    }

    override suspend fun deleteWarehouse(id: String) {
        val result = retryWithBackoff(
            shouldRetry = { shouldRetry(it) }
        ) {
            safeApiCall {
                client.from(tableName)
                    .delete {
                        filter {
                            eq("id", id)
                        }
                    }
            }
        }
        result.getOrThrow()
    }
}
