package data.remote.datasource.supabase

import data.remote.SupabaseClientProvider
import data.remote.datasource.VehicleRemoteDataSource
import data.remote.dto.response.VehicleDto
import data.remote.dto.request.VehicleCreateRequestDto
import data.remote.dto.request.VehicleUpdateRequestDto
import data.remote.utils.safeApiCall
import io.github.jan.supabase.postgrest.from

class VehicleRemoteDataSourceImpl : VehicleRemoteDataSource {

    private val client = SupabaseClientProvider.client
    private val tableName = "vehicles"

    override suspend fun getAllVehicles(): List<VehicleDto> {
        return safeApiCall {
            client.from(tableName)
                .select()
                .decodeList<VehicleDto>()
        }
    }

    override suspend fun getVehicleById(id: String): VehicleDto? {
        return safeApiCall {
            client.from(tableName)
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<VehicleDto>()
        }
    }

    override suspend fun createVehicle(
        vehicleRequest: VehicleCreateRequestDto
    ): VehicleDto {
        return safeApiCall {
            client.from(tableName)
                .insert(vehicleRequest) {
                    select()
                }
                .decodeSingle<VehicleDto>()
        }
    }

    override suspend fun updateVehicle(
        id: String,
        vehicleRequest: VehicleUpdateRequestDto
    ): VehicleDto {
        return safeApiCall {
            client.from(tableName)
                .update(vehicleRequest) {
                    filter {
                        eq("id", id)
                    }
                    select()
                }
                .decodeSingle<VehicleDto>()
        }
    }

    override suspend fun deleteVehicle(id: String) {
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
