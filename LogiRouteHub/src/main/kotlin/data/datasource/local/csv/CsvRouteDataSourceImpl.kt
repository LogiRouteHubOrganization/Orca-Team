
package data.datasource.local.csv

import data.dataholder.RouteRaw
import data.datasource.local.LocalRouteDataSource
import data.exception.local.LocalDataException
import data.parser.RouteParser
import data.validation.LocalDataSourceValidator
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CsvRouteDataSourceImpl(
    private val routeFile: File,
    private val routeParser: RouteParser,
    private val localDataSourceValidator: LocalDataSourceValidator
) : LocalRouteDataSource {

    private val headerLineCount = 1

    private val csvHeader =
        "routeId,originHubId,destinationHubId,distanceKm,typicalDelayMin"

    override suspend fun loadRoutes(): List<RouteRaw> =
        withContext(Dispatchers.IO) {
            loadRoutesFromCsv()
        }

    override suspend fun addRoute(route: RouteRaw) {
        withContext(Dispatchers.IO) {
            val routes = loadRoutesFromCsv().toMutableList()

            routes.add(route)

            writeRoutesToCsv(routes)
        }
    }

    override suspend fun updateRoute(route: RouteRaw) {
        withContext(Dispatchers.IO) {
            val routes = loadRoutesFromCsv().toMutableList()

            val routeIndex = routes.indexOfFirst { currentRoute ->
                currentRoute.routeId == route.routeId
            }

            if (routeIndex == -1) {
                throw LocalDataException(
                    "Route '${route.routeId}' was not found in local data"
                )
            }

            routes[routeIndex] = route

            writeRoutesToCsv(routes)
        }
    }

    override suspend fun deleteRoute(routeId: String) {
        withContext(Dispatchers.IO) {
            val routes = loadRoutesFromCsv().toMutableList()

            val wasRemoved = routes.removeIf { route ->
                route.routeId == routeId
            }

            if (!wasRemoved) {
                throw LocalDataException(
                    "Route '$routeId' was not found in local data"
                )
            }
            writeRoutesToCsv(routes)
        }
    }

    override suspend fun replaceRoutes(routes: List<RouteRaw>) {
        withContext(Dispatchers.IO) {
            writeRoutesToCsv(routes)
        }
    }

    private fun loadRoutesFromCsv(): List<RouteRaw> {
        localDataSourceValidator.validate(routeFile)

        val lines = routeFile.readLines()

        if (lines.size <= headerLineCount) {
            throw LocalDataException(
                "CSV file contains no data rows: ${routeFile.name}"
            )
        }
        return lines
            .drop(headerLineCount)
            .mapIndexedNotNull { index, row ->
                routeParser.parseRouteRow(
                    row = row,
                    rowNumber = index + headerLineCount
                )
            }
    }

    private fun writeRoutesToCsv(routes: List<RouteRaw>) {
        val lines = buildList {
            add(csvHeader)

            addAll(
                routes.map { route ->
                    routeParser.toCsvRow(route)
                }
            )
        }
        routeFile.writeText(lines.joinToString("\n"))
    }
}
