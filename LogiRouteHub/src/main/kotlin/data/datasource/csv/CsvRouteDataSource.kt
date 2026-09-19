package data.datasource.csv

import data.dataholder.RouteRaw
import data.datasource.RouteDataSource
import data.exception.LocalDataException
import data.parser.RouteParser
import data.validation.CsvFileValidator
import java.io.File

class CsvRouteDataSource(
    private val routeFile: File,
    private val routeParser: RouteParser,
    private val csvFileValidator: CsvFileValidator
) : RouteDataSource {

    private val headerLineCount = 1

    override fun loadRoutes(): List<RouteRaw> {
        csvFileValidator.validate(routeFile)

        val lines = routeFile.readLines()

        if (lines.size <= headerLineCount) {
            throw LocalDataException(
                "CSV file contains no data rows: ${routeFile.name}"
            )
        }

        val dataRows = lines.drop(headerLineCount)

        return dataRows.mapIndexed { index, row ->
            routeParser.parseRouteRow(
                row = row,
                rowNumber = index + headerLineCount
            ) ?: throw LocalDataException(
                "Invalid route row at line ${index + headerLineCount + 1}"
            )
        }
    }
}