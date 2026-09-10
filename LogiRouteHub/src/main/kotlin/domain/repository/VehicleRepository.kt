package domain.repository

import domain.model.Vehicle

interface VehicleRepository {

    fun findById(vehicleId: String): Vehicle?

    fun findAll(): List<Vehicle>

    fun add(vehicle: Vehicle)

    fun update(vehicle: Vehicle)
}
