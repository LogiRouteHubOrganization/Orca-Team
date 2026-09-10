package data.datasource

import data.dataholder.FleetRaw

interface VehicleDataSource {

    fun loadVehicles(): List<FleetRaw>
}
