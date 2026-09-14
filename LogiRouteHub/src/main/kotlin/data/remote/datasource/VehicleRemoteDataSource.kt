package data.remote.datasource

import data.remote.dto.VehicleDto
import data.remote.dto.request.VehicleRequestDto

interface VehicleRemoteDataSource {
    suspend fun getAllVehicles(): List<VehicleDto>
    suspend fun getVehicleById(id: String): VehicleDto?
    suspend fun createVehicle(vehicleRequest: VehicleRequestDto): VehicleDto
    suspend fun updateVehicle(id: String, vehicleRequest: VehicleRequestDto): VehicleDto
    suspend fun deleteVehicle(id: String)
}
