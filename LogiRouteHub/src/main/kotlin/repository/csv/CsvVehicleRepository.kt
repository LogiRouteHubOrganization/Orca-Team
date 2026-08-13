package repository.csv

import dataHolder.FleetRaw
import parser.FleetParser
import repository.VehicleRepository
import java.io.File

private const val HEADER_LINE_COUNT = 1

class CsvVehicleRepository(
    private val vehicleFile: File,
    private val fleetParser: FleetParser
) : VehicleRepository {

    override fun loadVehicles(): List<FleetRaw> {
        if (!vehicleFile.exists()) {
            println(
                "Diagnostic Warning: File ${vehicleFile.name} does not exist."
            )
            return emptyList()
        }

        val vehicleRows = vehicleFile.readLines()
        val dataRows = vehicleRows.drop(HEADER_LINE_COUNT)
        val validVehicles = mutableListOf<FleetRaw>()

        for (vehicleRow in dataRows) {
            val parsedVehicle =
                fleetParser.createFleetRow(vehicleRow)

            if (parsedVehicle != null) {
                validVehicles.add(parsedVehicle)
            }
        }

        return validVehicles
    }
}