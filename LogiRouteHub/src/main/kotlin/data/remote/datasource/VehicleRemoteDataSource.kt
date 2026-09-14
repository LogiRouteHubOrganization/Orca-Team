package data.remote.datasource

import data.remote.dto.VehicleDto
import data.remote.dto.request.VehicleCreateRequestDto
import data.remote.dto.request.VehicleUpdateRequestDto

interface VehicleRemoteDataSource {

    suspend fun getAllVehicles(): List<VehicleDto>

    suspend fun getVehicleById(id: String): VehicleDto?

    suspend fun createVehicle(
        vehicleRequest: VehicleCreateRequestDto
    ): VehicleDto

    suspend fun updateVehicle(
        id: String,
        vehicleRequest: VehicleUpdateRequestDto
    ): VehicleDto

    suspend fun deleteVehicle(id: String)
}
