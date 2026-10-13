
package data.datasource.local

import data.dataholder.FleetRaw

interface LocalVehicleDataSource {

    suspend fun loadVehicles(): List<FleetRaw>

    suspend fun addVehicle(vehicle: FleetRaw)

    suspend fun updateVehicle(vehicle: FleetRaw)

    suspend fun deleteVehicle(vehicleId: String)

    suspend fun replaceVehicles(vehicles: List<FleetRaw>)
}
