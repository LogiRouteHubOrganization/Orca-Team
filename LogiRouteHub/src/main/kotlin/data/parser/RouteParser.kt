
package data.parser

import data.dataholder.RouteRaw

class RouteParser {

    private companion object {
        const val CSV_SEPARATOR = ","
        const val EXPECTED_COLUMN_COUNT = 5
        const val ROUTE_ID_INDEX = 0
        const val ORIGIN_HUB_ID_INDEX = 1
        const val DESTINATION_HUB_ID_INDEX = 2
        const val DISTANCE_INDEX = 3
        const val DELAY_INDEX = 4
        const val REQUIRED_ID_DIGITS = 6
    }

    fun parseRouteRow(
        row: String
    ): CsvRowParseResult<RouteRaw> {
        val values = row.split(CSV_SEPARATOR).map(String::trim)

        if (values.size != EXPECTED_COLUMN_COUNT) {
            return CsvRowParseResult.Failure(
                "Expected $EXPECTED_COLUMN_COUNT columns, " +
                        "but found ${values.size}."
            )
        }

        if (values.any(String::isBlank)) {
            return CsvRowParseResult.Failure(
                "Route row contains an empty value."
            )
        }

        val routeId = normalizeIdentifier(values[ROUTE_ID_INDEX])
        val originHubId = normalizeIdentifier(values[ORIGIN_HUB_ID_INDEX])
        val destinationHubId =
            normalizeIdentifier(values[DESTINATION_HUB_ID_INDEX])

        val distanceResult = parseDistanceKm(values[DISTANCE_INDEX])
        if (distanceResult is CsvRowParseResult.Failure) {
            return distanceResult
        }

        val distanceKm =
            (distanceResult as CsvRowParseResult.Success).value

        val delayResult = parseTypicalDelayMin(values[DELAY_INDEX])
        if (delayResult is CsvRowParseResult.Failure) {
            return delayResult
        }

        val typicalDelayMin =
            (delayResult as CsvRowParseResult.Success).value

        return CsvRowParseResult.Success(
            RouteRaw(
                routeId = routeId,
                originHubId = originHubId,
                destinationHubId = destinationHubId,
                distanceKm = distanceKm,
                typicalDelayMin = typicalDelayMin
            )
        )
    }

    private fun parseDistanceKm(
        value: String
    ): CsvRowParseResult<Double> {
        val distance = value.toDoubleOrNull()

        if (distance == null || !distance.isFinite() || distance <= 0.0) {
            return CsvRowParseResult.Failure(
                "Distance must be a finite positive number."
            )
        }

        return CsvRowParseResult.Success(distance)
    }

    private fun parseTypicalDelayMin(
        value: String
    ): CsvRowParseResult<Int> {
        val delay = value.toIntOrNull()

        if (delay == null || delay < 0) {
            return CsvRowParseResult.Failure(
                "Typical delay must be a non-negative integer."
            )
        }

        return CsvRowParseResult.Success(delay)
    }

    private fun normalizeIdentifier(identifier: String): String {
        val normalized = identifier.trim().uppercase()
        val parts = normalized.split("-")

        if (parts.size != 2) return normalized

        val prefix = parts[0]
        val numericPart = parts[1]

        if (prefix.isBlank() || numericPart.toIntOrNull() == null) {
            return normalized
        }

        return "$prefix-${numericPart.padStart(REQUIRED_ID_DIGITS, '0')}"
    }

    fun toCsvRow(route: RouteRaw): String =
        listOf(
            route.routeId,
            route.originHubId,
            route.destinationHubId,
            route.distanceKm,
            route.typicalDelayMin
        ).joinToString(CSV_SEPARATOR)
}
