package repository.csv

import dataholder.FleetRaw
import parser.FleetParser
import repository.VehicleRepository
import java.io.File


class CsvVehicleRepository(
    private val vehicleFile: File,
    private val fleetParser: FleetParser
) : VehicleRepository {
    private val headerLineCount = 1

    override fun loadVehicles(): List<FleetRaw> {
        if (!vehicleFile.exists()) {
            println("Warning: Fleet CSV file does not exist at ${vehicleFile.path}")
            return emptyList()
        }

        return vehicleFile.readLines()
            .drop(headerLineCount)
            .mapNotNull { row -> fleetParser.parseFleetRow(row) }
    }
}
