
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
    }

    fun parseRouteRow(
        row: String
    ): CsvRowParseResult<RouteRaw> {
        val values = row.split(CSV_SEPARATOR).map(String::trim)

        if (values.size != EXPECTED_COLUMN_COUNT) {
            return CsvRowParseResult.Failure(
                "Expected $EXPECTED_COLUMN_COUNT columns, " + "but found ${values.size}.")
        }

        if (values.any(String::isBlank)) {
            return CsvRowParseResult.Failure("Route row contains an empty value.")
        }
        val routeId = IdentifierNormalizer.normalize(values[ROUTE_ID_INDEX])
        val originHubId = IdentifierNormalizer.normalize(values[ORIGIN_HUB_ID_INDEX])
        val destinationHubId = IdentifierNormalizer.normalize(values[DESTINATION_HUB_ID_INDEX])

        val distanceResult = parseDistanceKm(values[DISTANCE_INDEX])
        if (distanceResult is CsvRowParseResult.Failure) {
            return distanceResult
        }

        val distanceKm = (distanceResult as CsvRowParseResult.Success).value
        val delayResult = parseTypicalDelayMin(values[DELAY_INDEX])
        if (delayResult is CsvRowParseResult.Failure) {
            return delayResult
        }

        val typicalDelayMin = (delayResult as CsvRowParseResult.Success).value
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
            return CsvRowParseResult.Failure("Typical delay must be a non-negative integer.")
        }
        return CsvRowParseResult.Success(delay)
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
