
package data.datasource.remote.supabase

import data.datasource.remote.RemoteWarehouseDataSource
import data.remote.dto.response.WarehouseDto
import data.remote.dto.request.WarehouseCreateRequestDto
import data.remote.dto.request.WarehouseUpdateRequestDto
import data.remote.utils.safeApiCall
import data.remote.retry.RetryableRemoteDatasource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseWarehouseDataSourceImpl(
    private val client: SupabaseClient,
    private val retryTemplate: RetryableRemoteDatasource
) : RemoteWarehouseDataSource {

    private val tableName = "warehouses"

    override suspend fun getAllWarehouses(): List<WarehouseDto> =
        withContext(Dispatchers.IO) {
            val result = retryTemplate.execute(operationName = "GetAllWarehouses") {
                safeApiCall {
                    client.from(tableName).select().decodeList<WarehouseDto>()
                }
            }
            result.getOrThrow()
        }

    override suspend fun getWarehouseById(
        id: String
    ): WarehouseDto? = withContext(Dispatchers.IO) {
        val result = retryTemplate.execute(operationName = "GetWarehouseById") {
            safeApiCall {
                client.from(tableName).select {
                        filter { eq("id", id) }
                    }.decodeSingleOrNull<WarehouseDto>()
            }
        }
        result.getOrThrow()
    }

    override suspend fun createWarehouse(
        warehouseRequest: WarehouseCreateRequestDto
    ): WarehouseDto = withContext(Dispatchers.IO) {
        val result = retryTemplate.execute(
            operationName = "CreateWarehouse"
        ) {
            safeApiCall {
                client.from(tableName).insert(warehouseRequest) {
                        select()
                    }.decodeSingle<WarehouseDto>()
            }
        }
        result.getOrThrow()
    }

    override suspend fun updateWarehouse(
        id: String,
        warehouseRequest: WarehouseUpdateRequestDto
    ): WarehouseDto = withContext(Dispatchers.IO) {
        val result = retryTemplate.execute(operationName = "UpdateWarehouse") {
            safeApiCall {
                client.from(tableName).update(warehouseRequest) {
                        filter { eq("id", id) }
                        select()
                    }
                    .decodeSingle<WarehouseDto>()
            }
        }

        result.getOrThrow()
    }

    override suspend fun deleteWarehouse(id: String) {
        withContext(Dispatchers.IO) {
            val result = retryTemplate.execute(operationName = "DeleteWarehouse") {
                safeApiCall {
                    client.from(tableName).delete {
                            filter { eq("id", id) }
                        }
                }
            }
            result.getOrThrow()
        }
    }
}
