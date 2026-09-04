package domain.repository

import domain.model.Vehicle

interface VehicleRepository {

    fun findById(vehicleId: String): Vehicle?

    fun update(vehicle: Vehicle)
}
