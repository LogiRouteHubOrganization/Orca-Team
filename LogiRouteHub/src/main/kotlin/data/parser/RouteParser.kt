package data.parser

import data.dataholder.RouteRaw

class RouteParser {

    private val csvSeparator = ","
    private val expectedColumnCount = 5
    private val routeIdColumnIndex = 0
    private val originHubIdColumnIndex = 1
    private val destinationHubIdColumnIndex = 2
    private val distanceColumnIndex = 3
    private val delayColumnIndex = 4
    private val requiredIdDigits = 6

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
            .split(csvSeparator)
            .map { value ->
                value.trim()
            }
    }

    private fun hasValidColumnCount(
        values: List<String>,
        rowNumber: Int
    ): Boolean {

        if (values.size != expectedColumnCount) {
            println(
                "Warning: Row ${rowNumber + 1} " +
                        "has wrong number of columns"
            )
            return false
        }

        return true
    }

    private fun hasNoEmptyValues(
        values: List<String>,
        rowNumber: Int
    ): Boolean {

        if (values.any { value -> value.isEmpty() }) {
            println(
                "Warning: Row ${rowNumber + 1} " +
                        "contains an empty value"
            )
            return false
        }

        return true
    }

    private fun createRouteRaw(
        values: List<String>,
        rowNumber: Int
    ): RouteRaw? {

        val distanceKm = parseDistanceKm(
            values[distanceColumnIndex],
            rowNumber
        )

        if (distanceKm == null) return null

        val typicalDelayMin = parseTypicalDelayMin(
            values[delayColumnIndex],
            rowNumber
        )

        if (typicalDelayMin == null) return null

        return RouteRaw(
            routeId = normalizeIdentifier(
                values[routeIdColumnIndex]
            ),
            originHubId = normalizeIdentifier(
                values[originHubIdColumnIndex]
            ),
            destinationHubId = normalizeIdentifier(
                values[destinationHubIdColumnIndex]
            ),
            distanceKm = distanceKm,
            typicalDelayMin = typicalDelayMin
        )
    }

    private fun parseDistanceKm(
        distance: String,
        rowNumber: Int
    ): Double? {

        val distanceKm = distance
            .trim()
            .toDoubleOrNull()

        if (distanceKm == null || distanceKm <= 0) {
            println(
                "Warning: Row ${rowNumber + 1} " +
                        "has invalid distance"
            )
            return null
        }

        return distanceKm
    }

    private fun parseTypicalDelayMin(
        delay: String,
        rowNumber: Int
    ): Int? {

        val typicalDelayMin = delay
            .trim()
            .toIntOrNull()

        if (typicalDelayMin == null || typicalDelayMin < 0) {
            println(
                "Warning: Row ${rowNumber + 1} " +
                        "has invalid delay"
            )
            return null
        }

        return typicalDelayMin
    }

    private fun normalizeIdentifier(
        identifier: String
    ): String {

        val normalizedIdentifier = identifier
            .trim()
            .uppercase()

        val identifierParts = normalizedIdentifier.split("-")

        if (identifierParts.size != 2) {
            return normalizedIdentifier
        }

        val prefix = identifierParts[0]
        val numericPart = identifierParts[1]

        if (numericPart.toIntOrNull() == null) {
            return normalizedIdentifier
        }

        val normalizedNumericPart = numericPart.padStart(
            length = requiredIdDigits,
            padChar = '0'
        )

        return "$prefix-$normalizedNumericPart"
    }
}
