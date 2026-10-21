package data.datasource.remote.supabase

import data.datasource.remote.RemoteVehicleDataSource
import data.remote.circuitbreaker.CircuitBreaker
import data.remote.dto.response.VehicleDto
import data.remote.dto.request.VehicleCreateRequestDto
import data.remote.dto.request.VehicleUpdateRequestDto
import data.remote.ratelimit.RateLimiter
import data.remote.utils.safeApiCall
import data.remote.retry.RetryableRemoteDatasource
import dispatcher.AppDispatchers
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.withContext

class SupabaseVehicleDataSourceImpl(
    private val client: SupabaseClient,
    private val retryTemplate: RetryableRemoteDatasource,
    private val circuitBreaker: CircuitBreaker,
    private val appDispatchers: AppDispatchers,
    private val rateLimiter: RateLimiter
) : RemoteVehicleDataSource {

    private val tableName = "vehicles"

    override suspend fun getAllVehicles(): List<VehicleDto> =
        withContext(appDispatchers.io) {
            circuitBreaker.execute {
                val result = retryTemplate.execute(operationName = "GetAllVehicles") {
                    rateLimiter.acquire()
                    safeApiCall {
                        client.from(tableName).select().decodeList<VehicleDto>()
                    }
                }
                result.getOrThrow()
            }
        }

    override suspend fun getVehicleById(id: String): VehicleDto? =
        withContext(appDispatchers.io) {
            circuitBreaker.execute {
                val result = retryTemplate.execute(operationName = "GetVehicleById") {
                    rateLimiter.acquire()
                    safeApiCall {
                        client.from(tableName).select {
                            filter { eq("id", id) }
                        }.decodeSingleOrNull<VehicleDto>()
                    }
                }
                result.getOrThrow()
            }
        }

    override suspend fun createVehicle(
        vehicleRequest: VehicleCreateRequestDto
    ): VehicleDto = withContext(appDispatchers.io) {
        circuitBreaker.execute {
            val result = retryTemplate.execute(operationName = "CreateVehicle") {
                rateLimiter.acquire()
                safeApiCall {
                    client.from(tableName).insert(vehicleRequest) { select() }.decodeSingle<VehicleDto>()
                }
            }
            result.getOrThrow()
        }
    }

    override suspend fun updateVehicle(
        id: String,
        vehicleRequest: VehicleUpdateRequestDto
    ): VehicleDto = withContext(appDispatchers.io) {
        circuitBreaker.execute {
            val result = retryTemplate.execute(operationName = "UpdateVehicle") {
                rateLimiter.acquire()
                safeApiCall {
                    client.from(tableName)
                        .update(vehicleRequest) {
                            filter { eq("id", id) }
                            select()
                        }.decodeSingle<VehicleDto>()
                }
            }
            result.getOrThrow()
        }
    }

    override suspend fun deleteVehicle(id: String) {
        withContext(appDispatchers.io) {
            circuitBreaker.execute {
                val result = retryTemplate.execute(
                    operationName = "DeleteVehicle"
                ) {
                    rateLimiter.acquire()
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
}
