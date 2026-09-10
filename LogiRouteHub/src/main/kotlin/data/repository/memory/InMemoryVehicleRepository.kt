package data.repository.memory

import domain.model.Vehicle
import domain.repository.VehicleRepository

class InMemoryVehicleRepository(
    private val vehicles: MutableList<Vehicle> = mutableListOf()
) : VehicleRepository {

    override fun findById(vehicleId: String): Vehicle? {
        return vehicles.find { it.id == vehicleId }
    }

    override fun findAll(): List<Vehicle> {
        return vehicles
    }

    override fun add(vehicle: Vehicle) {
        vehicles.add(vehicle)
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
