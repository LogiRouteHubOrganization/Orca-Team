package repository.csv

import dataHolder.FleetRaw
import parser.FleetParser
import repository.VehicleRepository

class CsvVehicleRepository(
    private val filePath: String
) : VehicleRepository {

    private val fleetParser = FleetParser()

    override fun getAll(): List<FleetRaw> {
        return fleetParser.readFleetRowsFromFile(filePath)
    }
}