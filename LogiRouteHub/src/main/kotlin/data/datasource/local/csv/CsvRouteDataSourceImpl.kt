
package data.datasource.local.csv

import data.dataholder.RouteRaw
import data.datasource.local.LocalRouteDataSource
import data.exception.local.LocalDataException
import data.parser.RouteParser
import dispatcher.AppDispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CsvRouteDataSourceImpl(
    private val routeFile: File,
    private val routeParser: RouteParser,
    private val csvFileLoader: CsvFileLoader,
    private val appDispatchers: AppDispatchers
) : LocalRouteDataSource {

    private companion object {
        const val HEADER_LINE_COUNT = 1
        const val CSV_HEADER =
            "routeId,originHubId,destinationHubId,distanceKm,typicalDelayMin"
    }

    override suspend fun loadRoutes(): List<RouteRaw> =
        withContext(appDispatchers.io) {
            loadRoutesFromCsv()
        }

    override suspend fun addRoute(route: RouteRaw) {
        withContext(appDispatchers.io) {
            val routes = loadRoutesFromCsv().toMutableList()
            routes.add(route)
            writeRoutesToCsv(routes)
        }
    }

    override suspend fun updateRoute(route: RouteRaw) {
        withContext(appDispatchers.io) {
            val routes = loadRoutesFromCsv().toMutableList()

            val index = routes.indexOfFirst {
                it.routeId == route.routeId
            }

            if (index == -1) {
                throw LocalDataException(
                    "Route '${route.routeId}' was not found in local data."
                )
            }

            routes[index] = route
            writeRoutesToCsv(routes)
        }
    }

    override suspend fun deleteRoute(routeId: String) {
        withContext(appDispatchers.io) {
            val routes = loadRoutesFromCsv().toMutableList()

            val removed = routes.removeIf {
                it.routeId == routeId
            }

            if (!removed) {
                throw LocalDataException(
                    "Route '$routeId' was not found in local data."
                )
            }

            writeRoutesToCsv(routes)
        }
    }

    override suspend fun replaceRoutes(routes: List<RouteRaw>) {
        withContext(appDispatchers.io) {
            writeRoutesToCsv(routes)
        }
    }

    private fun loadRoutesFromCsv(): List<RouteRaw> =
        csvFileLoader.load(
            file = routeFile,
            parseRow = routeParser::parseRouteRow,
            headerLineCount = HEADER_LINE_COUNT
        )

    private fun writeRoutesToCsv(routes: List<RouteRaw>) {
        val content = buildList {
            add(CSV_HEADER)
            addAll(routes.map(routeParser::toCsvRow))
        }.joinToString(System.lineSeparator())

        routeFile.writeText(content)
    }
}
