package domain.repository

import domain.model.Vehicle
import domain.model.updateinput.VehicleUpdateModel

interface VehicleRepository {

    suspend fun findById(vehicleId: String): Vehicle?

    suspend fun findAll(): List<Vehicle>

    suspend fun add(vehicle: Vehicle): Vehicle

    suspend fun update( vehicleId: String, updateModel: VehicleUpdateModel): Vehicle

    suspend fun delete(vehicleId: String)
}
