package data.datasource.csv

import data.dataholder.WarehouseRaw
import data.datasource.WarehouseDataSource
import data.parser.WarehouseParser
import java.io.File


class CsvWarehouseDataSource(
    private val warehouseFile: File,
    private val warehouseParser: WarehouseParser
) : WarehouseDataSource {

    private val headerLineCount = 1

    override fun loadWarehouses(): List<WarehouseRaw> {

        if (!warehouseFile.exists()) {
            println("Diagnostic Warning: " + "File ${warehouseFile.name} does not exist.")
            return emptyList()
        }

        val warehouseRows = warehouseFile.readLines()

        val dataRows = warehouseRows.drop(headerLineCount)

        val validWarehouses = mutableListOf<WarehouseRaw>()

        for (warehouseRow in dataRows) {

            val parsedWarehouse = warehouseParser.parseWarehouseRow(warehouseRow)

            if (parsedWarehouse != null) {
                validWarehouses.add(parsedWarehouse)
            }
        }
        return validWarehouses
    }
}
