
package domain.repository

import domain.model.Vehicle
import domain.model.updateinput.VehicleUpdateModel

interface VehicleRepository {

    suspend fun findById(vehicleId: String): Result<Vehicle?>

    suspend fun findAll(): Result<List<Vehicle>>

    suspend fun add(vehicle: Vehicle): Result<Vehicle>

    suspend fun update(
        vehicleId: String,
        updateModel: VehicleUpdateModel
    ): Result<Vehicle>

    suspend fun delete(vehicleId: String): Result<Unit>
}