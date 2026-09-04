package data.repository

import data.dataholder.FleetRaw

interface VehicleRepository {

    fun loadVehicles(): List<FleetRaw>
}
