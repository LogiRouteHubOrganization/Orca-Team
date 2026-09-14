package domain.repository

import domain.model.Vehicle

interface VehicleRepository {

    suspend fun findById(vehicleId: String): Vehicle?

    suspend fun findAll(): List<Vehicle>

    suspend fun add(vehicle: Vehicle)

    suspend fun update(vehicle: Vehicle)

    suspend fun delete(vehicleId: String)
}
