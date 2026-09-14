package data.remote.datasource

import data.remote.SupabaseClientProvider
import data.remote.dto.VehicleDto
import data.remote.dto.request.VehicleRequestDto
import data.remote.utils.safeApiCall
import io.github.jan.supabase.postgrest.from

class VehicleRemoteDataSourceImpl(
    private val tableName: String = "vehicles"
) : VehicleRemoteDataSource {

    private val client = SupabaseClientProvider.client

    override suspend fun getAllVehicles(): List<VehicleDto> =
        safeApiCall {
            client
                .from(tableName)
                .select()
                .decodeList<VehicleDto>()
        }

    override suspend fun getVehicleById(id: String): VehicleDto? =
        safeApiCall {
            client
                .from(tableName)
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<VehicleDto>()
        }

    override suspend fun createVehicle(
        vehicleRequest: VehicleRequestDto
    ): VehicleDto =
        safeApiCall {
            client
                .from(tableName)
                .insert(vehicleRequest) {
                    select()
                }
                .decodeSingle<VehicleDto>()
        }

    override suspend fun updateVehicle(
        id: String,
        vehicleRequest: VehicleRequestDto
    ): VehicleDto =
        safeApiCall {
            client
                .from(tableName)
                .update(vehicleRequest) {
                    filter {
                        eq("id", id)
                    }
                    select()
                }
                .decodeSingle<VehicleDto>()
        }

    override suspend fun deleteVehicle(id: String) {
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