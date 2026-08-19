package parser

import dataholder.FleetRaw


class FleetParser {

    private val CSV_SEPARATOR = ","
    private val EXPECTED_COLUMN_COUNT = 4
    private val VEHICLE_ID_COLUMN_INDEX = 0
    private val CURRENT_HUB_ID_COLUMN_INDEX = 1
    private val MAX_CAPACITY_COLUMN_INDEX = 2
    private val COST_PER_KM_COLUMN_INDEX = 3

    fun parseFleetRow(
        fleetCsvRow: String
    ): FleetRaw? {
        val columns = fleetCsvRow.split(CSV_SEPARATOR)

        if (hasInvalidColumnCount(columns)) {
            printInvalidFleetRowWarning(fleetCsvRow)
            return null
        }
        val vehicleId = normalizeIdentifier(
            columns[VEHICLE_ID_COLUMN_INDEX]
        )
        val currentHubId = normalizeIdentifier(columns[CURRENT_HUB_ID_COLUMN_INDEX])
        if (hasEmptyRequiredField(vehicleId, currentHubId)) {
            printEmptyFleetFieldWarning(fleetCsvRow)
            return null
        }
        val maxCapacityKg = parseMaxCapacityKg(
            columns[MAX_CAPACITY_COLUMN_INDEX],
            fleetCsvRow
        ) ?: return null
        val costPerKm = parseCostPerKm(
            columns[COST_PER_KM_COLUMN_INDEX],
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
        return columns.size != EXPECTED_COLUMN_COUNT
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
