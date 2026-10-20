
package data.parser

import data.dataholder.FleetRaw

class FleetParser {

    private companion object {
        const val CSV_SEPARATOR = ","
        const val EXPECTED_COLUMN_COUNT = 4
        const val VEHICLE_ID_INDEX = 0
        const val CURRENT_HUB_ID_INDEX = 1
        const val MAX_CAPACITY_INDEX = 2
        const val COST_PER_KM_INDEX = 3
    }

    fun parseFleetRow(
        row: String
    ): CsvRowParseResult<FleetRaw> {
        val columns = row.split(CSV_SEPARATOR).map(String::trim)

        if (columns.size != EXPECTED_COLUMN_COUNT) {
            return CsvRowParseResult.Failure(
                "Expected $EXPECTED_COLUMN_COUNT columns, " +
                        "but found ${columns.size}."
            )
        }

        val vehicleId = IdentifierNormalizer.normalize(columns[VEHICLE_ID_INDEX])
        val currentHubId = IdentifierNormalizer.normalize(columns[CURRENT_HUB_ID_INDEX])

        if (vehicleId.isBlank() || currentHubId.isBlank()) {
            return CsvRowParseResult.Failure(
                "Vehicle ID and current hub ID must not be blank."
            )
        }
        val capacityResult = parseMaxCapacityKg(columns[MAX_CAPACITY_INDEX])

        if (capacityResult is CsvRowParseResult.Failure) { return capacityResult }

        val maxCapacityKg = (capacityResult as CsvRowParseResult.Success).value

        val costResult = parseCostPerKm(columns[COST_PER_KM_INDEX])

        if (costResult is CsvRowParseResult.Failure) { return costResult }

        val costPerKm = (costResult as CsvRowParseResult.Success).value

        return CsvRowParseResult.Success(
            FleetRaw(
                vehicleId = vehicleId,
                currentHubId = currentHubId,
                maxCapacityKg = maxCapacityKg,
                costPerKm = costPerKm
            )
        )
    }

    private fun parseMaxCapacityKg(
        value: String
    ): CsvRowParseResult<Double> {
        val capacity = value.toDoubleOrNull()

        if (capacity == null || !capacity.isFinite() || capacity <= 0.0) {
            return CsvRowParseResult.Failure(
                "Vehicle capacity must be a finite positive number."
            )
        }

        return CsvRowParseResult.Success(capacity)
    }

    private fun parseCostPerKm(
        value: String
    ): CsvRowParseResult<Double> {
        val cost = value.toDoubleOrNull()

        if (cost == null || !cost.isFinite() || cost <= 0.0) {
            return CsvRowParseResult.Failure(
                "Vehicle cost per kilometer must be a finite positive number."
            )
        }

        return CsvRowParseResult.Success(cost)
    }

    fun toCsvRow(vehicle: FleetRaw): String =
        listOf(
            vehicle.vehicleId,
            vehicle.currentHubId,
            vehicle.maxCapacityKg,
            vehicle.costPerKm
        ).joinToString(CSV_SEPARATOR)
}
