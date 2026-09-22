package data.remote.datasource.supabase

import data.remote.SupabaseClientProvider
import data.remote.datasource.WarehouseRemoteDataSource
import data.remote.dto.response.WarehouseDto
import data.remote.dto.request.WarehouseCreateRequestDto
import data.remote.dto.request.WarehouseUpdateRequestDto
import data.remote.utils.safeApiCall
import data.remote.retry.RetryableRemoteDatasource
import io.github.jan.supabase.postgrest.from

class WarehouseRemoteDataSourceImpl(
    private val retryTemplate: RetryableRemoteDatasource = RetryableRemoteDatasource()
) : WarehouseRemoteDataSource {

    private val client = SupabaseClientProvider.client
    private val tableName = "warehouses"

    override suspend fun getAllWarehouses(): List<WarehouseDto> {
        val result = retryTemplate.execute(
            operationName = "GetAllWarehouses"
        ) {
            safeApiCall {
                client.from(tableName).select()
                    .decodeList<WarehouseDto>()
            }
        }

        return result.getOrThrow()
    }

    override suspend fun getWarehouseById(
        id: String
    ): WarehouseDto? {
        val result = retryTemplate.execute(
            operationName = "GetWarehouseById"
        ) {
            safeApiCall {
                client.from(tableName).select {
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
        val result = retryTemplate.execute(
            operationName = "CreateWarehouse"
        ) {
            safeApiCall {
                client.from(tableName).insert(warehouseRequest) {
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
        val result = retryTemplate.execute(
            operationName = "UpdateWarehouse"
        ) {
            safeApiCall {
                client.from(tableName).update(warehouseRequest) {
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
        val result = retryTemplate.execute(
            operationName = "DeleteWarehouse"
        ) {
            safeApiCall {
                client.from(tableName).delete {
                        filter {
                            eq("id", id)
                        }
                    }
            }
        }

        result.getOrThrow()
    }
}
