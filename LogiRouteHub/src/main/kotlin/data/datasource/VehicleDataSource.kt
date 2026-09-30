package data.datasource

import data.dataholder.FleetRaw

interface VehicleDataSource {

    fun loadVehicles(): List<FleetRaw>

    fun addVehicle(vehicle: FleetRaw)

    fun updateVehicle(vehicle: FleetRaw)

    fun deleteVehicle(vehicleId: String)

    fun replaceVehicles(vehicles: List<FleetRaw>)
}
