package repository.csv

import dataHolder.FleetRaw
import parser.FleetParser
import repository.VehicleRepository

class CsvVehicleRepository(
    private val filePath: String,
    private val fleetParser: FleetParser
) : VehicleRepository {

    override fun loadVehicles(): List<FleetRaw> {
        return fleetParser.readFleetRowsFromFile(filePath)
    }
}