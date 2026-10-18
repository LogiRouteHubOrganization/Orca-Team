
package data.datasource.local.csv

import data.dataholder.WarehouseRaw
import data.datasource.local.LocalWarehouseDataSource
import data.exception.local.LocalDataException
import data.parser.WarehouseParser
import dispatcher.AppDispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CsvWarehouseDataSourceImpl(
    private val warehouseFile: File,
    private val warehouseParser: WarehouseParser,
    private val csvFileLoader: CsvFileLoader,
    private val appDispatchers: AppDispatchers
) : LocalWarehouseDataSource {

    private companion object {
        const val HEADER_LINE_COUNT = 1
        const val CSV_HEADER =
            "id,name,regional_zone,latitude,longitude"
    }

    override suspend fun loadWarehouses(): List<WarehouseRaw> =
        withContext(appDispatchers.io) {
            loadWarehousesFromCsv()
        }

    override suspend fun addWarehouse(warehouse: WarehouseRaw) {
        withContext(appDispatchers.io) {
            val warehouses = loadWarehousesFromCsv().toMutableList()
            warehouses.add(warehouse)
            writeWarehousesToCsv(warehouses)
        }
    }

    override suspend fun updateWarehouse(warehouse: WarehouseRaw) {
        withContext(appDispatchers.io) {
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
        withContext(appDispatchers.io) {
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
        withContext(appDispatchers.io) {
            writeWarehousesToCsv(warehouses)
        }
    }

    private fun loadWarehousesFromCsv(): List<WarehouseRaw> =
        csvFileLoader.load(
            file = warehouseFile,
            parseRow = warehouseParser::parseWarehouseRow,
            headerLineCount = HEADER_LINE_COUNT
        )

    private fun writeWarehousesToCsv(
        warehouses: List<WarehouseRaw>
    ) {
        val content = buildList {
            add(CSV_HEADER)
            addAll(warehouses.map(warehouseParser::toCsvRow))
        }.joinToString(System.lineSeparator())

        warehouseFile.writeText(content)
    }
}
