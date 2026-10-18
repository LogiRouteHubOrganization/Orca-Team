
package data.parser

import data.dataholder.WarehouseRaw

class WarehouseParser {

    private companion object {
        const val CSV_SEPARATOR = ","
        const val EXPECTED_COLUMN_COUNT = 5
        const val WAREHOUSE_ID_INDEX = 0
        const val WAREHOUSE_NAME_INDEX = 1
        const val REGIONAL_ZONE_INDEX = 2
        const val LATITUDE_INDEX = 3
        const val LONGITUDE_INDEX = 4
        const val REQUIRED_ID_DIGITS = 6
    }

    fun parseWarehouseRow(
        row: String
    ): CsvRowParseResult<WarehouseRaw> {
        val columns = row.trim().split(CSV_SEPARATOR)

        if (columns.size != EXPECTED_COLUMN_COUNT) {
            return CsvRowParseResult.Failure(
                "Expected $EXPECTED_COLUMN_COUNT columns, " +
                        "but found ${columns.size}."
            )
        }

        val warehouseId =
            normalizeIdentifier(columns[WAREHOUSE_ID_INDEX])
        val warehouseName = columns[WAREHOUSE_NAME_INDEX].trim()
        val regionalZone =
            columns[REGIONAL_ZONE_INDEX].trim().uppercase()

        if (
            warehouseId.isBlank() ||
            warehouseName.isBlank() ||
            regionalZone.isBlank()
        ) {
            return CsvRowParseResult.Failure(
                "Warehouse ID, name, and regional zone must not be blank."
            )
        }

        val latitudeResult =
            parseCoordinate(columns[LATITUDE_INDEX], "latitude")

        if (latitudeResult is CsvRowParseResult.Failure) {
            return latitudeResult
        }

        val latitude =
            (latitudeResult as CsvRowParseResult.Success).value

        val longitudeResult =
            parseCoordinate(columns[LONGITUDE_INDEX], "longitude")

        if (longitudeResult is CsvRowParseResult.Failure) {
            return longitudeResult
        }

        val longitude =
            (longitudeResult as CsvRowParseResult.Success).value

        if (latitude !in -90.0..90.0) {
            return CsvRowParseResult.Failure(
                "Latitude must be between -90 and 90."
            )
        }

        if (longitude !in -180.0..180.0) {
            return CsvRowParseResult.Failure(
                "Longitude must be between -180 and 180."
            )
        }

        return CsvRowParseResult.Success(
            WarehouseRaw(
                warehouseId = warehouseId,
                warehouseName = warehouseName,
                regionalZone = regionalZone,
                latitude = latitude,
                longitude = longitude
            )
        )
    }

    private fun parseCoordinate(
        value: String,
        coordinateName: String
    ): CsvRowParseResult<Double> {
        val coordinate = value.trim().toDoubleOrNull()

        if (coordinate == null || !coordinate.isFinite()) {
            return CsvRowParseResult.Failure(
                "Invalid $coordinateName: '$value'."
            )
        }

        return CsvRowParseResult.Success(coordinate)
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

    fun toCsvRow(warehouse: WarehouseRaw): String =
        listOf(
            warehouse.warehouseId,
            warehouse.warehouseName,
            warehouse.regionalZone,
            warehouse.latitude,
            warehouse.longitude
        ).joinToString(CSV_SEPARATOR)
}
