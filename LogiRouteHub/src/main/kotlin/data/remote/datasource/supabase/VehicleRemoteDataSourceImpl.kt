package data.remote.datasource.supabase

import data.remote.SupabaseClientProvider
import data.remote.datasource.VehicleRemoteDataSource
import data.remote.dto.response.VehicleDto
import data.remote.dto.request.VehicleCreateRequestDto
import data.remote.dto.request.VehicleUpdateRequestDto
import data.remote.utils.safeApiCall
import data.remote.retry.RetryableRemoteDatasource
import io.github.jan.supabase.postgrest.from

class VehicleRemoteDataSourceImpl(
    private val retryTemplate: RetryableRemoteDatasource = RetryableRemoteDatasource()
) : VehicleRemoteDataSource {

    private val client = SupabaseClientProvider.client
    private val tableName = "vehicles"

    override suspend fun getAllVehicles(): List<VehicleDto> {
        val result = retryTemplate.execute(
            operationName = "GetAllVehicles"
        ) {
            safeApiCall {
                client.from(tableName).select()
                    .decodeList<VehicleDto>()
            }
        }

        return result.getOrThrow()
    }

    override suspend fun getVehicleById(id: String): VehicleDto? {
        val result = retryTemplate.execute(
            operationName = "GetVehicleById"
        ) {
            safeApiCall {
                client.from(tableName).select {
                        filter {
                            eq("id", id)
                        }
                    }
                    .decodeSingleOrNull<VehicleDto>()
            }
        }

        return result.getOrThrow()
    }

    override suspend fun createVehicle(
        vehicleRequest: VehicleCreateRequestDto
    ): VehicleDto {
        val result = retryTemplate.execute(
            operationName = "CreateVehicle"
        ) {
            safeApiCall {
                client.from(tableName).insert(vehicleRequest) {
                        select()
                    }
                    .decodeSingle<VehicleDto>()
            }
        }

        return result.getOrThrow()
    }

    override suspend fun updateVehicle(
        id: String,
        vehicleRequest: VehicleUpdateRequestDto
    ): VehicleDto {
        val result = retryTemplate.execute(
            operationName = "UpdateVehicle"
        ) {
            safeApiCall {
                client.from(tableName).update(vehicleRequest) {
                        filter {
                            eq("id", id)
                        }
                        select()
                    }
                    .decodeSingle<VehicleDto>()
            }
        }

        return result.getOrThrow()
    }

    override suspend fun deleteVehicle(id: String) {
        val result = retryTemplate.execute(
            operationName = "DeleteVehicle"
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
