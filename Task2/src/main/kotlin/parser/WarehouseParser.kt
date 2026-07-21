package parser

import java.io.File
import model.Warehouse

private const val EXPECTED_COLUMN_COUNT = 3
private const val COLUMN_INDEX_ID = 0
private const val COLUMN_INDEX_NAME = 1
private const val COLUMN_INDEX_RegionalZone = 2
private const val HEADER_LINE_COUNT = 1

private fun isColumnCountInvalid(columns: List<String>): Boolean {
    return columns.size != EXPECTED_COLUMN_COUNT
}

private fun areFieldsEmpty(warehouseId: String, warehousename: String, regionalZone: String): Boolean {
    return warehouseId.isEmpty() ||warehousename.isEmpty() || regionalZone.isEmpty()
}

private fun buildWarehouseFromColumns(columns: List<String>, originalRow: String): Warehouse? {
    val warehouseId = columns[COLUMN_INDEX_ID].trim()
    val warehousename = columns[COLUMN_INDEX_NAME].trim()
    val regionalZone = columns[COLUMN_INDEX_RegionalZone].trim()

    if (areFieldsEmpty(warehouseId , warehousename, regionalZone)) {
        println("Diagnostic Warning: Skipping row with empty fields: '$originalRow'")
        return null
    }
    return Warehouse(warehouseId = warehouseId , warehousename = warehousename, regionalZone = regionalZone)
}

private fun parseWarehouseRow(row: String): Warehouse? {
    val trimmedRow = row.trim()

    if (trimmedRow.isEmpty()) {
        return null
    }
    val columns = trimmedRow.split(",")

    if (isColumnCountInvalid(columns)) {
        println("Diagnostic Warning: Skipping malformed row due to missing columns: '$row'")
        return null
    }
    return buildWarehouseFromColumns(columns, row)
}

fun parseWarehousesFile(warehouseFile: File): List<Warehouse> {
    val warehouses = mutableListOf<Warehouse>()

    if (!warehouseFile.exists()) {
        println("Diagnostic Warning: File ${warehouseFile.name} does not exist.")
        return warehouses
    }

    val WarehouseRows = warehouseFile.readLines()
    val dataRows = WarehouseRows.drop(HEADER_LINE_COUNT)

    for (warehouseRow in dataRows) {
        val warehouse = parseWarehouseRow(warehouseRow)

        if (warehouse != null) {
            warehouses.add(warehouse)
        }
    }
    return warehouses
}

