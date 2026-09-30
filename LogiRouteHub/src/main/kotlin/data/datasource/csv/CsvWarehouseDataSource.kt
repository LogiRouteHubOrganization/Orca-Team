package data.datasource.csv

import data.dataholder.WarehouseRaw
import data.datasource.WarehouseDataSource
import data.exception.local.LocalDataException
import data.parser.WarehouseParser
import data.validation.LocalDataSourceValidator
import java.io.File

class CsvWarehouseDataSource(
    private val warehouseFile: File,
    private val warehouseParser: WarehouseParser,
    private val localDataSourceValidator: LocalDataSourceValidator
) : WarehouseDataSource {

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
}
