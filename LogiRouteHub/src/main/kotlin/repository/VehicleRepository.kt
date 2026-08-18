package repository

import dataholder.FleetRaw

interface VehicleRepository {

    fun loadVehicles(): List<FleetRaw>
}
