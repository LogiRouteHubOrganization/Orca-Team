package parser

import dataholder.FleetRaw


class FleetParser {

    private val csvSeparator = ","
    private val expectedColumnCount = 4
    private val vehicleIdColumnIndex = 0
    private val currentHubIdColumnIndex = 1
    private val maxCapacityColumnIndex = 2
    private val costPerKmColumnIndex = 3

    fun parseFleetRow(
        fleetCsvRow: String
    ): FleetRaw? {
        val columns = fleetCsvRow.split(csvSeparator)

        if (hasInvalidColumnCount(columns)) {
            printInvalidFleetRowWarning(fleetCsvRow)
            return null
        }
        val vehicleId = normalizeIdentifier(
            columns[vehicleIdColumnIndex]
        )
        val currentHubId = normalizeIdentifier(columns[currentHubIdColumnIndex])
        if (hasEmptyRequiredField(vehicleId, currentHubId)) {
            printEmptyFleetFieldWarning(fleetCsvRow)
            return null
        }
        val maxCapacityKg = parseMaxCapacityKg(
            columns[maxCapacityColumnIndex],
            fleetCsvRow
        ) ?: return null
        val costPerKm = parseCostPerKm(
            columns[costPerKmColumnIndex],
            fleetCsvRow
        ) ?: return null

        return FleetRaw(
            vehicleId = vehicleId,
            currentHubId = currentHubId,
            maxCapacityKg = maxCapacityKg,
            costPerKm = costPerKm
        )
    }

    private fun hasInvalidColumnCount(columns: List<String>): Boolean {
        return columns.size != expectedColumnCount
    }

    private fun hasEmptyRequiredField(vehicleId: String, currentHubId: String): Boolean {
        return vehicleId.isEmpty() || currentHubId.isEmpty()
    }

    private fun parseMaxCapacityKg(capacity: String, fleetCsvRow: String): Double? {
        val maxCapacityKg = capacity.trim().toDoubleOrNull()
        if (maxCapacityKg == null || maxCapacityKg <= 0) {
            println("Warning: Invalid vehicle capacity -> $fleetCsvRow")
            return null
        }
        return maxCapacityKg
    }

    private fun parseCostPerKm(cost: String, fleetCsvRow: String): Double? {
        val costPerKm = cost.trim().toDoubleOrNull()
        if (costPerKm == null || costPerKm < 0) {
            println("Warning: Invalid vehicle cost -> $fleetCsvRow")
            return null
        }
        return costPerKm
    }

    private fun normalizeIdentifier(identifier: String): String {
        return identifier.trim().uppercase()
    }

    private fun printInvalidFleetRowWarning(fleetCsvRow: String) {
        println("Warning: Invalid fleet row -> $fleetCsvRow")
    }

    private fun printEmptyFleetFieldWarning(fleetCsvRow: String) {
        println("Warning: Empty required fleet field -> $fleetCsvRow")
    }
}
