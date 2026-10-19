package data.datasource.local.csv

import data.dataholder.WarehouseRaw
import data.datasource.local.LocalWarehouseDataSource
import data.exception.local.LocalDataException
import data.parser.WarehouseParser
import data.validation.LocalDataSourceValidator
import java.io.File

class CsvWarehouseDataSourceImpl(
    private val warehouseFile: File,
    private val warehouseParser: WarehouseParser,
    private val localDataSourceValidator: LocalDataSourceValidator
) : LocalWarehouseDataSource {

    private val headerLineCount = 1

    override fun loadWarehouses(): List<WarehouseRaw> {
        localDataSourceValidator.validate(warehouseFile)

        val warehouseRows = warehouseFile.readLines()

        if (warehouseRows.size <= headerLineCount) {
            throw LocalDataException(
                "CSV file contains no data rows: ${warehouseFile.name}"
            )
        }

        val dataRows = warehouseRows.drop(headerLineCount)

        return dataRows.mapNotNull { row ->
            warehouseParser.parseWarehouseRow(row)
        }
    }

    override fun addWarehouse(
        warehouse: WarehouseRaw
    ) {
        val warehouses = loadWarehouses().toMutableList()

        warehouses.add(warehouse)

        writeWarehouses(warehouses)
    }

    override fun updateWarehouse(
        warehouse: WarehouseRaw
    ) {
        val warehouses = loadWarehouses().toMutableList()

        val index = warehouses.indexOfFirst {
            it.warehouseId == warehouse.warehouseId
        }

        if (index == -1) {
            throw LocalDataException(
                "Warehouse not found: ${warehouse.warehouseId}"
            )
        }

        warehouses[index] = warehouse

        writeWarehouses(warehouses)
    }

    override fun deleteWarehouse(
        warehouseId: String
    ) {
        val warehouses = loadWarehouses().toMutableList()

        val removed = warehouses.removeIf { it.warehouseId == warehouseId }

        if (!removed) {
            throw LocalDataException(
                "Warehouse not found: $warehouseId"
            )
        }

        writeWarehouses(warehouses)
    }

    override fun replaceWarehouses(
        warehouses: List<WarehouseRaw>
    ) {
        writeWarehouses(warehouses)
    }

    private fun writeWarehouses(
        warehouses: List<WarehouseRaw>
    ) {
        val header = "id,name,regional_zone,latitude,longitude"

        val rows = warehouses.map(
            warehouseParser::toCsvRow
        )

        val content = buildList {
            add(header)
            addAll(rows)
        }.joinToString(System.lineSeparator())

        warehouseFile.writeText(content)
    }
}
