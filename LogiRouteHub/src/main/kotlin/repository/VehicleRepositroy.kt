package repository

import dataHolder.FleetRaw

interface VehicleRepository {

    fun getAll(): List<FleetRaw>
}