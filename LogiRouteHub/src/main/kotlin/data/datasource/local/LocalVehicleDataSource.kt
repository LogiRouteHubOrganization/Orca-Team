package data.datasource.local

import data.dataholder.FleetRaw

interface LocalVehicleDataSource {

    fun loadVehicles(): List<FleetRaw>

    fun addVehicle(vehicle: FleetRaw)

    fun updateVehicle(vehicle: FleetRaw)

    fun deleteVehicle(vehicleId: String)

    fun replaceVehicles(vehicles: List<FleetRaw>)
}
