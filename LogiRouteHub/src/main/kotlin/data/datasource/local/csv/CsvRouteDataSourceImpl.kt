package data.datasource.csv

import data.dataholder.RouteRaw
import data.datasource.local.LocalRouteDataSource
import data.exception.local.LocalDataException
import data.parser.RouteParser
import data.validation.LocalDataSourceValidator
import java.io.File

class CsvRouteDataSourceImpl(
    private val routeFile: File,
    private val routeParser: RouteParser,
    private val localDataSourceValidator: LocalDataSourceValidator
) : LocalRouteDataSource {

    private val headerLineCount = 1
    private val csvHeader =
        "routeId,originHubId,destinationHubId,distanceKm,typicalDelayMin"

    override fun loadRoutes(): List<RouteRaw> {
        localDataSourceValidator.validate(routeFile)

        val lines = routeFile.readLines()

        if (lines.size <= headerLineCount) {
            throw LocalDataException(
                "CSV file contains no data rows: ${routeFile.name}"
            )
        }

        val dataRows = lines.drop(headerLineCount)

        return dataRows.mapIndexedNotNull { index, row ->
            routeParser.parseRouteRow(
                row = row,
                rowNumber = index + headerLineCount
            )
        }
    }

    override fun addRoute(route: RouteRaw) {
        val routes = loadRoutes().toMutableList()

        routes.add(route)

        writeRoutes(routes)
    }

    override fun updateRoute(route: RouteRaw) {
        val routes = loadRoutes().toMutableList()

        val routeIndex = routes.indexOfFirst { currentRoute ->
            currentRoute.routeId == route.routeId
        }

        if (routeIndex == -1) {
            throw LocalDataException(
                "Route '${route.routeId}' was not found in local data"
            )
        }

        routes[routeIndex] = route

        writeRoutes(routes)
    }

    override fun deleteRoute(routeId: String) {
        val routes = loadRoutes().toMutableList()

        val wasRemoved = routes.removeIf { route ->
            route.routeId == routeId
        }

        if (!wasRemoved) {
            throw LocalDataException(
                "Route '$routeId' was not found in local data"
            )
        }

        writeRoutes(routes)
    }

    override fun replaceRoutes(routes: List<RouteRaw>) {
        writeRoutes(routes)
    }

    private fun writeRoutes(routes: List<RouteRaw>) {
        val lines = buildList {
            add(csvHeader)

            addAll(
                routes.map { route ->
                    routeParser.toCsvRow(route)
                }
            )
        }
        routeFile.writeText(
            lines.joinToString("\n")
        )
    }
}
