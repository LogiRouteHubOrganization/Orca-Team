package data.datasource.csv

import data.dataholder.RouteRaw
import data.datasource.RouteDataSource
import data.parser.RouteParser
import java.io.File


class CsvRouteDataSource(
    private val routeFile: File,
    private val routeParser: RouteParser
) : RouteDataSource {
    private val headerLineCount = 1

    override fun loadRoutes(): List<RouteRaw> {

        if (!routeFile.exists()) {
            println("Diagnostic Warning: " + "File ${routeFile.name} does not exist.")
            return emptyList()
        }
        val lines = routeFile.readLines()
        val validRoutes = mutableListOf<RouteRaw>()

        for ((rowNumber, line) in lines.withIndex()) {

            if (rowNumber < headerLineCount) continue

            val parsedRoute = routeParser.parseRouteRow(
                row = line,
                rowNumber = rowNumber
            )
            if (parsedRoute != null) {
                validRoutes.add(parsedRoute)
            }
        }
        return validRoutes
    }
}
