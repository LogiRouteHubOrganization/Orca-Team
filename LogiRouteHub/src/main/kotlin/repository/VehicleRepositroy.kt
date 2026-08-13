package repository

import dataHolder.FleetRaw

interface VehicleRepository {

    fun loadVehicles(): List<FleetRaw>
}