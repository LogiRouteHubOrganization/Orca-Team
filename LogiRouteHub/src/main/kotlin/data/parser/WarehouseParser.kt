package data.parser

import data.dataholder.WarehouseRaw


class WarehouseParser {
    private val csvSeparator = ","
    private val expectedColumnCount = 5
    private val warehouseIdColumnIndex = 0
    private val warehouseNameColumnIndex = 1
    private val regionalZoneColumnIndex = 2
    private val latitudeColumnIndex = 3
    private val longitudeColumnIndex = 4

    fun parseWarehouseRow(row: String): WarehouseRaw? {
        val trimmedRow = row.trim()
        if (trimmedRow.isEmpty()) {
            return null
        }
        val columns = trimmedRow.split(csvSeparator)

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
        return columns.size != expectedColumnCount
    }

    private fun buildWarehouseFromColumns(
        columns: List<String>,
        originalRow: String
    ): WarehouseRaw? {
        val warehouseId = normalizeIdentifier(columns[warehouseIdColumnIndex])
        val warehouseName = columns[warehouseNameColumnIndex].trim()
        val regionalZone = columns[regionalZoneColumnIndex].trim().uppercase()
        val latitudeText = columns[latitudeColumnIndex].trim()
        val longitudeText = columns[longitudeColumnIndex].trim()

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
