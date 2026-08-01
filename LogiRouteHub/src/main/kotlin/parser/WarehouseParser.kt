package parser

import dataHolder.warehouseRaw
import java.io.File

private const val EXPECTED_COLUMN_COUNT = 5

private const val COLUMN_INDEX_ID = 0
private const val COLUMN_INDEX_NAME = 1
private const val COLUMN_INDEX_REGIONAL_ZONE = 2
private const val COLUMN_INDEX_LATITUDE = 3
private const val COLUMN_INDEX_LONGITUDE = 4

private const val HEADER_LINE_COUNT = 1

private fun isColumnCountInvalid(
    columns: List<String>
): Boolean {

    return columns.size != EXPECTED_COLUMN_COUNT
}

private fun areFieldsEmpty(
    warehouseId: String,
    warehousename: String,
    regionalZone: String,
    latitude: String,
    longitude: String
): Boolean {

    return warehouseId.isEmpty() ||
            warehousename.isEmpty() ||
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
        println(
            "Diagnostic Warning: Invalid $coordinateName: '$originalRow'"
        )

        return null
    }

    return coordinateValue
}

private fun buildWarehouseFromColumns(
    columns: List<String>,
    originalRow: String
): warehouseRaw? {

    val warehouseId =
        columns[COLUMN_INDEX_ID].trim()

    val warehousename =
        columns[COLUMN_INDEX_NAME].trim()

    val regionalZone =
        columns[COLUMN_INDEX_REGIONAL_ZONE].trim()

    val latitudeText =
        columns[COLUMN_INDEX_LATITUDE].trim()

    val longitudeText =
        columns[COLUMN_INDEX_LONGITUDE].trim()

    if (
        areFieldsEmpty(
            warehouseId,
            warehousename,
            regionalZone,
            latitudeText,
            longitudeText
        )
    ) {
        println(
            "Diagnostic Warning: Skipping row with empty fields: '$originalRow'"
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

    return warehouseRaw(
        warehouseId = warehouseId,
        warehousename = warehousename,
        regionalZone = regionalZone,
        latitude = latitude,
        longitude = longitude
    )
}

private fun parseWarehouseRow(
    row: String
): warehouseRaw? {

    val trimmedRow = row.trim()

    if (trimmedRow.isEmpty()) {
        return null
    }

    val columns = trimmedRow.split(",")

    if (isColumnCountInvalid(columns)) {
        println(
            "Diagnostic Warning: Skipping malformed row due to invalid column count: '$row'"
        )

        return null
    }

    return buildWarehouseFromColumns(
        columns,
        row
    )
}

fun parseWarehousesFile(
    warehouseFile: File
): List<warehouseRaw> {

    val warehouses = mutableListOf<warehouseRaw>()

    if (!warehouseFile.exists()) {
        println(
            "Diagnostic Warning: File ${warehouseFile.name} does not exist."
        )

        return warehouses
    }

    val warehouseRows = warehouseFile.readLines()

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