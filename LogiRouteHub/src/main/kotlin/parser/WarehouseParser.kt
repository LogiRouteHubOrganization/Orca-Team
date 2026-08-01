package parser

import dataHolder.WarehouseRaw
import java.io.File

private const val CSV_SEPARATOR = ","
private const val EXPECTED_COLUMN_COUNT = 5
private const val HEADER_LINE_COUNT = 1

private const val WAREHOUSE_ID_COLUMN_INDEX = 0
private const val WAREHOUSE_NAME_COLUMN_INDEX = 1
private const val REGIONAL_ZONE_COLUMN_INDEX = 2
private const val LATITUDE_COLUMN_INDEX = 3
private const val LONGITUDE_COLUMN_INDEX = 4

private fun hasInvalidColumnCount(
    columns: List<String>
): Boolean {

    return columns.size != EXPECTED_COLUMN_COUNT
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

    val coordinateValue =
        coordinate.trim().toDoubleOrNull()

    if (coordinateValue == null) {
        println(
            "Diagnostic Warning: " +
                    "Invalid $coordinateName -> '$originalRow'"
        )

        return null
    }

    return coordinateValue
}

private fun buildWarehouseFromColumns(
    columns: List<String>,
    originalRow: String
): WarehouseRaw? {

    val warehouseId =
        normalizeIdentifier(
            columns[WAREHOUSE_ID_COLUMN_INDEX]
        )

    val warehouseName =
        columns[WAREHOUSE_NAME_COLUMN_INDEX].trim()

    val regionalZone =
        columns[REGIONAL_ZONE_COLUMN_INDEX]
            .trim()
            .uppercase()

    val latitudeText =
        columns[LATITUDE_COLUMN_INDEX].trim()

    val longitudeText =
        columns[LONGITUDE_COLUMN_INDEX].trim()

    if (
        hasEmptyWarehouseField(
            warehouseId,
            warehouseName,
            regionalZone,
            latitudeText,
            longitudeText
        )
    ) {
        println(
            "Diagnostic Warning: " +
                    "Skipping row with empty fields -> '$originalRow'"
        )

        return null
    }

    val latitude = parseCoordinate(
        latitudeText,
        "latitude",
        originalRow
    )

    if (latitude == null) {
        return null
    }

    val longitude = parseCoordinate(
        longitudeText,
        "longitude",
        originalRow
    )

    if (longitude == null) {
        return null
    }

    return WarehouseRaw(
        warehouseId = warehouseId,
        warehouseName = warehouseName,
        regionalZone = regionalZone,
        latitude = latitude,
        longitude = longitude
    )
}

private fun parseWarehouseRow(
    row: String
): WarehouseRaw? {

    val trimmedRow = row.trim()

    if (trimmedRow.isEmpty()) {
        return null
    }

    val columns =
        trimmedRow.split(CSV_SEPARATOR)

    if (hasInvalidColumnCount(columns)) {
        println(
            "Diagnostic Warning: " +
                    "Invalid warehouse column count -> '$row'"
        )

        return null
    }

    return buildWarehouseFromColumns(
        columns,
        row
    )
}

private fun normalizeIdentifier(
    identifier: String
): String {

    return identifier.trim().uppercase()
}

fun parseWarehousesFile(
    warehouseFile: File
): List<WarehouseRaw> {

    val warehouses = mutableListOf<WarehouseRaw>()

    if (!warehouseFile.exists()) {
        println(
            "Diagnostic Warning: " +
                    "File ${warehouseFile.name} does not exist."
        )

        return warehouses
    }

    val warehouseRows =
        warehouseFile.readLines()

    val dataRows =
        warehouseRows.drop(HEADER_LINE_COUNT)

    for (warehouseRow in dataRows) {

        val warehouse =
            parseWarehouseRow(warehouseRow)

        if (warehouse != null) {
            warehouses.add(warehouse)
        }
    }

    return warehouses
}