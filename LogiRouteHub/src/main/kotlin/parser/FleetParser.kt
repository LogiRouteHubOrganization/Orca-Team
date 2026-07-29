package parser

import dataHolder.FleetRaw
import java.io.File

class FleetParser {

    fun readFleetRowsFromFile(filePath: String): List<FleetRaw> {

        val fleetCsvRows = readFleetCsvRows(filePath)

        return fleetCsvRows
            .drop(1)
            .filter { fleetCsvRow ->
                fleetCsvRow.isNotBlank()
            }
            .mapNotNull { fleetCsvRow ->
                createFleetRow(fleetCsvRow)
            }
    }

    private fun readFleetCsvRows(filePath: String): List<String> {
        return File(filePath).readLines()
    }

    private fun createFleetRow(fleetCsvRow: String): FleetRaw? {

        val expectedColumnCount = 4

        val vehicleIdIndex = 0
        val currentHubIdIndex = 1
        val maxCapacityIndex = 2
        val costPerKmIndex = 3

        val columns = fleetCsvRow.split(",")

        if (columns.size != expectedColumnCount) {
            println("Warning: Invalid fleet row -> $fleetCsvRow")
            return null
        }

        val vehicleIds =
            columns[vehicleIdIndex].trim()

        val currentHubId =
            columns[currentHubIdIndex].trim()

        val maxCapacityKg =
            columns[maxCapacityIndex]
                .trim()
                .toDoubleOrNull()

        val costPerKm =
            columns[costPerKmIndex]
                .trim()
                .toDoubleOrNull()

        if (maxCapacityKg == null || costPerKm == null) {
            println("Warning: Invalid numeric value -> $fleetCsvRow")
            return null
        }

        return FleetRaw(
            vehicleIds = vehicleIds,
            currentHubId = currentHubId,
            maxCapacityKg = maxCapacityKg,
            costPerKm = costPerKm)
    }
}