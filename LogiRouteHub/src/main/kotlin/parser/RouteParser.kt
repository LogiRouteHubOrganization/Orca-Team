package parser

import dataholder.RouteRaw

private const val CSV_SEPARATOR = ","
private const val EXPECTED_COLUMN_COUNT = 5

private const val ROUTE_ID_COLUMN_INDEX = 0
private const val ORIGIN_HUB_ID_COLUMN_INDEX = 1
private const val DESTINATION_HUB_ID_COLUMN_INDEX = 2
private const val DISTANCE_COLUMN_INDEX = 3
private const val DELAY_COLUMN_INDEX = 4

class RouteParser {

    fun parseRouteRow(
        row: String,
        rowNumber: Int
    ): RouteRaw? {

        if (row.isBlank()) return null

        val values = getRouteValues(row)

        if (!hasValidColumnCount(values, rowNumber)) return null

        if (!hasNoEmptyValues(values, rowNumber)) return null

        return createRouteRaw(
            values,
            rowNumber
        )
    }

    private fun getRouteValues(
        line: String
    ): List<String> {
        return line
            .split(CSV_SEPARATOR)
            .map { value ->
                value.trim()
            }
    }

    private fun hasValidColumnCount(
        values: List<String>,
        rowNumber: Int
    ): Boolean {

        if (values.size != EXPECTED_COLUMN_COUNT) {
            println("Warning: Row ${rowNumber + 1} " + "has wrong number of columns")
            return false
        }
        return true
    }

    private fun hasNoEmptyValues(
        values: List<String>,
        rowNumber: Int
    ): Boolean {
        if (values.any { value -> value.isEmpty() }) {
            println("Warning: Row ${rowNumber + 1} " + "contains an empty value")
            return false
        }

        return true
    }

    private fun createRouteRaw(
        values: List<String>,
        rowNumber: Int
    ): RouteRaw? {

        val distanceKm = parseDistanceKm(
            values[DISTANCE_COLUMN_INDEX],
            rowNumber
        )
        if (distanceKm == null) return null

        val typicalDelayMin = parseTypicalDelayMin(
            values[DELAY_COLUMN_INDEX],
            rowNumber
        )
        if (typicalDelayMin == null) return null

        return RouteRaw(
            routeId = normalizeIdentifier(
                values[ROUTE_ID_COLUMN_INDEX]
            ),
            originHubId = normalizeIdentifier(
                values[ORIGIN_HUB_ID_COLUMN_INDEX]
            ),
            destinationHubId = normalizeIdentifier(
                values[DESTINATION_HUB_ID_COLUMN_INDEX]
            ),
            distanceKm = distanceKm,
            typicalDelayMin = typicalDelayMin
        )
    }

    private fun parseDistanceKm(
        distance: String,
        rowNumber: Int
    ): Double? {
        val distanceKm = distance.toDoubleOrNull()
        if (distanceKm == null || distanceKm <= 0) {
            println("Warning: Row ${rowNumber + 1} " + "has invalid distance")
            return null
        }
        return distanceKm
    }

    private fun parseTypicalDelayMin(
        delay: String,
        rowNumber: Int
    ): Int? {
        val typicalDelayMin = delay.toIntOrNull()

        if (typicalDelayMin == null || typicalDelayMin < 0) {
            println("Warning: Row ${rowNumber + 1} " + "has invalid delay")
            return null
        }
        return typicalDelayMin
    }

    private fun normalizeIdentifier(
        identifier: String
    ): String {
        return identifier
            .trim()
            .uppercase()
    }
}
