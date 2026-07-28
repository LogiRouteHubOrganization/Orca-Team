package parser

import dataHolder.FleetRaw
import java.io.File

private const val EXPECTED_FLEET_COLUMN_COUNT = 4
private const val VEHICLE_ID_COLUMN_INDEX = 0
private const val CURRENT_HUB_ID_COLUMN_INDEX = 1
private const val MAX_CAPACITY_COLUMN_INDEX = 2
private const val COST_PER_KM_COLUMN_INDEX = 3

class FleetParser {

    fun readFleetRowsFromFile(filePath: String): List<FleetRaw> {
        val file = File(filePath)
        if (!file.exists()) return emptyList()
        return file.useLines { lines -> lines.drop(1)
                .filter { it.isNotBlank() }
                .mapNotNull { parseFleetRow(it) }
                .toList()
        }
    }
    private fun isValidFleetData(vehicleId: String, currentHubId: String,
                                 maxCapacityKg: Double?, costPerKm: Double?): Boolean {
        return vehicleId.isNotBlank() && currentHubId.isNotBlank() &&
                maxCapacityKg != null && costPerKm != null}


    private fun parseFleetRow(fleetLine: String): FleetRaw? {
        val fleetColumns = fleetLine.split(",")
        if (fleetColumns.size != EXPECTED_FLEET_COLUMN_COUNT) return null

        val vehicleId = fleetColumns[VEHICLE_ID_COLUMN_INDEX].trim()
        val currentHubId = fleetColumns[CURRENT_HUB_ID_COLUMN_INDEX].trim().uppercase()
        val maxCapacityKg = fleetColumns[MAX_CAPACITY_COLUMN_INDEX].trim().toDoubleOrNull()
        val costPerKm = fleetColumns[COST_PER_KM_COLUMN_INDEX].trim().toDoubleOrNull()

        if (!isValidFleetData(vehicleId, currentHubId, maxCapacityKg, costPerKm)) {
            return null
        }
        return FleetRaw(
            vehicleId = vehicleId,
            currentHubId = currentHubId,
            maxCapacityKg = maxCapacityKg!!,
            costPerKm = costPerKm!!)
    }

}