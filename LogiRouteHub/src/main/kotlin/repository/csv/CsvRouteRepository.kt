package repository.csv

import dataHolder.RouteRaw
import parser.RouteParser
import repository.RouteRepository
import java.io.File

private const val HEADER_ROW_COUNT = 1

class CsvRouteRepository(
    private val filePath: String,
    private val routeParser: RouteParser
) : RouteRepository {

    override fun loadRoutes(): List<RouteRaw> {

        val file = File(filePath)

        if (!file.exists()) {
            println(
                "Diagnostic Warning: File $filePath does not exist."
            )
            return emptyList()
        }

        val routes = mutableListOf<RouteRaw>()
        val lines = file.readLines()

        for ((rowNumber, line) in lines.withIndex()) {

            if (rowNumber < HEADER_ROW_COUNT) {
                continue
            }

            val route = routeParser.parseRouteRow(
                row = line,
                rowNumber = rowNumber
            )

            if (route != null) {
                routes.add(route)
            }
        }

        return routes
    }
}