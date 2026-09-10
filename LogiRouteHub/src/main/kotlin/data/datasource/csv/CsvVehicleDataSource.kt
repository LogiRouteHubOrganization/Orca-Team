package data.datasource.csv

import data.dataholder.FleetRaw
import data.datasource.VehicleDataSource
import data.parser.FleetParser
import java.io.File


class CsvVehicleDataSource(
    private val vehicleFile: File,
    private val fleetParser: FleetParser
) : VehicleDataSource {
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
