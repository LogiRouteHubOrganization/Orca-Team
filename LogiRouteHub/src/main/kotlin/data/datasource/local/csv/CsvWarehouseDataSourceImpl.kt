
package data.datasource.local.csv

import data.dataholder.WarehouseRaw
import data.datasource.local.LocalWarehouseDataSource
import data.exception.local.LocalDataException
import data.parser.WarehouseParser
import data.validation.LocalDataSourceValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CsvWarehouseDataSourceImpl(
    private val warehouseFile: File,
    private val warehouseParser: WarehouseParser,
    private val localDataSourceValidator: LocalDataSourceValidator
) : LocalWarehouseDataSource {

    private val headerLineCount = 1

    private val csvHeader =
        "id,name,regional_zone,latitude,longitude"

    override suspend fun loadWarehouses(): List<WarehouseRaw> =
        withContext(Dispatchers.IO) {
            loadWarehousesFromCsv()
        }

    override suspend fun addWarehouse(warehouse: WarehouseRaw) {
        withContext(Dispatchers.IO) {
            val warehouses = loadWarehousesFromCsv().toMutableList()

            warehouses.add(warehouse)

            writeWarehousesToCsv(warehouses)
        }
    }

    override suspend fun updateWarehouse(warehouse: WarehouseRaw) {
        withContext(Dispatchers.IO) {
            val warehouses = loadWarehousesFromCsv().toMutableList()

            val index = warehouses.indexOfFirst {
                it.warehouseId == warehouse.warehouseId
            }

            if (index == -1) {
                throw LocalDataException(
                    "Warehouse not found: ${warehouse.warehouseId}"
                )
            }

            warehouses[index] = warehouse

            writeWarehousesToCsv(warehouses)
        }
    }

    override suspend fun deleteWarehouse(warehouseId: String) {
        withContext(Dispatchers.IO) {
            val warehouses = loadWarehousesFromCsv().toMutableList()

            val removed = warehouses.removeIf {
                it.warehouseId == warehouseId
            }

            if (!removed) {
                throw LocalDataException(
                    "Warehouse not found: $warehouseId"
                )
            }

            writeWarehousesToCsv(warehouses)
        }
    }

    override suspend fun replaceWarehouses(
        warehouses: List<WarehouseRaw>
    ) {
        withContext(Dispatchers.IO) {
            writeWarehousesToCsv(warehouses)
        }
    }

    private fun loadWarehousesFromCsv(): List<WarehouseRaw> {
        localDataSourceValidator.validate(warehouseFile)

        val warehouseRows = warehouseFile.readLines()

        if (warehouseRows.size <= headerLineCount) {
            throw LocalDataException(
                "CSV file contains no data rows: ${warehouseFile.name}"
            )
        }

        return warehouseRows
            .drop(headerLineCount)
            .mapNotNull(warehouseParser::parseWarehouseRow)
    }

    private fun writeWarehousesToCsv(
        warehouses: List<WarehouseRaw>
    ) {
        val rows = warehouses.map(warehouseParser::toCsvRow)

        val content = buildList {
            add(csvHeader)
            addAll(rows)
        }.joinToString(System.lineSeparator())

        warehouseFile.writeText(content)
    }
}
