package data.repository

import data.datasource.RouteDataSource
import data.mapper.RouteMapper
import domain.model.Route
import domain.repository.RouteRepository
import domain.repository.WarehouseRepository

class RouteRepositoryImpl(
    private val routeDataSource: RouteDataSource,
    private val routeMapper: RouteMapper,
    private val warehouseRepository: WarehouseRepository
) : RouteRepository {

    companion object {
        private const val NOT_FOUND_INDEX = -1
    }

    private val routes = mutableListOf<Route>()
    private var isLoaded = false

    private fun loadRoutesIfNeeded() {
        if (isLoaded) return
        val rawRoutes = routeDataSource.loadRoutes()
        for (rawRoute in rawRoutes) {
            val originWarehouse = warehouseRepository.findById(rawRoute.originHubId)
            val destinationWarehouse = warehouseRepository.findById(rawRoute.destinationHubId)

            if (originWarehouse != null && destinationWarehouse != null) {
                val route = routeMapper.mapToDomainModel(
                    rawRoute = rawRoute,
                    originWarehouse = originWarehouse,
                    destinationWarehouse = destinationWarehouse
                )
                routes.add(route)
                originWarehouse.addRoute(route)
            }
        }
        isLoaded = true
    }

    override fun findById(routeId: String): Route? {
        loadRoutesIfNeeded()
        return routes.find { it.id == routeId }
    }

    override fun findAll(): List<Route> {
        loadRoutesIfNeeded()
        return routes.toList()
    }

    override fun add(route: Route) {
        loadRoutesIfNeeded()
        routes.add(route)
        route.originWarehouse.addRoute(route)

    }

    override fun update(route: Route) {
        loadRoutesIfNeeded()

        val routeIndex = routes.indexOfFirst {
            it.id == route.id
        }
        if (routeIndex == NOT_FOUND_INDEX) return
        val previousRoute = routes[routeIndex]
        val previousOriginWarehouse = previousRoute.originWarehouse
        val newOriginWarehouse = route.originWarehouse

        if (previousOriginWarehouse.id == newOriginWarehouse.id) {
            previousOriginWarehouse.outgoingRoutes
                .indexOfFirst {
                    it.id == route.id
                }.takeIf {
                    it != NOT_FOUND_INDEX
                }?.let { warehouseRouteIndex ->
                    previousOriginWarehouse.updateRouteAtIndex(
                        warehouseRouteIndex,
                        route
                    )
                }
            routes[routeIndex] = route
            return
        }
        previousOriginWarehouse.outgoingRoutes
            .find {
                it.id == route.id
            }?.let { previousRouteInWarehouse ->
                previousOriginWarehouse.removeRoute(
                    previousRouteInWarehouse
                )
            }
        newOriginWarehouse.addRoute(route)
        routes[routeIndex] = route
    }
}
