package repository.csv

import dataholder.RouteRaw
import parser.RouteParser
import repository.RouteRepository
import java.io.File


class CsvRouteRepository(
    private val routeFile: File,
    private val routeParser: RouteParser
) : RouteRepository {
    private val HEADER_ROW_COUNT = 1
    override fun loadRoutes(): List<RouteRaw> {

        if (!routeFile.exists()) {
            println("Diagnostic Warning: " + "File ${routeFile.name} does not exist.")
            return emptyList()
        }
        val lines = routeFile.readLines()
        val validRoutes = mutableListOf<RouteRaw>()

        for ((rowNumber, line) in lines.withIndex()) {

            if (rowNumber < HEADER_ROW_COUNT) continue

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
