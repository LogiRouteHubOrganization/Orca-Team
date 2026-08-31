package data.repository.csv

import data.dataholder.WarehouseRaw
import data.repository.WarehouseRepository
import data.parser.WarehouseParser
import java.io.File


class CsvWarehouseRepository(
    private val warehouseFile: File,
    private val warehouseParser: WarehouseParser
) : WarehouseRepository {

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
