package data.datasource.memory

import domain.model.Vehicle
import domain.repository.VehicleRepository

class InMemoryVehicleRepository(
    private val vehicles: MutableList<Vehicle>
) : VehicleRepository {

    override fun findById(vehicleId: String): Vehicle? {
        return vehicles.find { it.id == vehicleId }
    }

    override fun update(vehicle: Vehicle) {

        val notFoundIndex = -1

        val vehicleIndex = vehicles.indexOfFirst {
            it.id == vehicle.id
        }

        if (vehicleIndex != notFoundIndex) {
            vehicles[vehicleIndex] = vehicle
        }
    }
}
