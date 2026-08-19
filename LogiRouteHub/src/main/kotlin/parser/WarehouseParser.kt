package parser

import dataholder.WarehouseRaw


class WarehouseParser {
    private val CSV_SEPARATOR = ","
    private val EXPECTED_COLUMN_COUNT = 5
    private val WAREHOUSE_ID_COLUMN_INDEX = 0
    private val WAREHOUSE_NAME_COLUMN_INDEX = 1
    private val REGIONAL_ZONE_COLUMN_INDEX = 2
    private val LATITUDE_COLUMN_INDEX = 3
    private val LONGITUDE_COLUMN_INDEX = 4

    fun parseWarehouseRow(row: String): WarehouseRaw? {
        val trimmedRow = row.trim()
        if (trimmedRow.isEmpty()) {
            return null
        }
        val columns = trimmedRow.split(CSV_SEPARATOR)

        if (hasInvalidColumnCount(columns)) {
            println("Diagnostic Warning: " + "Invalid warehouse column count -> '$row'")
            return null
        }
        return buildWarehouseFromColumns(
            columns = columns,
            originalRow = row
        )
    }

    private fun hasInvalidColumnCount(columns: List<String>): Boolean {
        return columns.size != EXPECTED_COLUMN_COUNT
    }

    private fun buildWarehouseFromColumns(
        columns: List<String>,
        originalRow: String
    ): WarehouseRaw? {
        val warehouseId = normalizeIdentifier(columns[WAREHOUSE_ID_COLUMN_INDEX])
        val warehouseName = columns[WAREHOUSE_NAME_COLUMN_INDEX].trim()
        val regionalZone = columns[REGIONAL_ZONE_COLUMN_INDEX].trim().uppercase()
        val latitudeText = columns[LATITUDE_COLUMN_INDEX].trim()
        val longitudeText = columns[LONGITUDE_COLUMN_INDEX].trim()

        if (hasEmptyWarehouseField(
                warehouseId = warehouseId,
                warehouseName = warehouseName,
                regionalZone = regionalZone,
                latitude = latitudeText,
                longitude = longitudeText
            )
        ) {
            println("Diagnostic Warning: " + "Skipping row with empty fields -> '$originalRow'")
            return null
        }
        val latitude = parseCoordinate(
            coordinate = latitudeText,
            coordinateName = "latitude",
            originalRow = originalRow
        ) ?: return null
        val longitude = parseCoordinate(
            coordinate = longitudeText,
            coordinateName = "longitude",
            originalRow = originalRow
        ) ?: return null
        return WarehouseRaw(
            warehouseId = warehouseId,
            warehouseName = warehouseName,
            regionalZone = regionalZone,
            latitude = latitude,
            longitude = longitude
        )
    }

    private fun hasEmptyWarehouseField(
        warehouseId: String,
        warehouseName: String,
        regionalZone: String,
        latitude: String,
        longitude: String
    ): Boolean {

        return warehouseId.isEmpty() ||
                warehouseName.isEmpty() ||
                regionalZone.isEmpty() ||
                latitude.isEmpty() ||
                longitude.isEmpty()
    }

    private fun parseCoordinate(
        coordinate: String,
        coordinateName: String,
        originalRow: String
    ): Double? {

        val coordinateValue = coordinate.trim().toDoubleOrNull()

        if (coordinateValue == null) {
            println("Diagnostic Warning: " + "Invalid $coordinateName -> '$originalRow'")
            return null
        }
        return coordinateValue
    }

    private fun normalizeIdentifier(
        identifier: String
    ): String {
        return identifier.trim().uppercase()
    }
}
