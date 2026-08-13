package repository.csv

import dataHolder.FleetRaw
import parser.FleetParser
import repository.VehicleRepository
import java.io.File

private const val HEADER_ROW_COUNT = 1

class CsvVehicleRepository(
    private val vehicleFile: File,
    private val fleetParser: FleetParser
) : VehicleRepository {

    override fun loadVehicles(): List<FleetRaw> {
        if (!vehicleFile.exists()) {
            println("Warning: Fleet CSV file does not exist at ${vehicleFile.path}")
            return emptyList()
        }

        return vehicleFile.readLines()
            .drop(HEADER_ROW_COUNT)
            .mapNotNull { row -> fleetParser.parseFleetRow(row) }
    }
}