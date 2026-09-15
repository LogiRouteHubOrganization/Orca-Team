package data.repository.local

import data.datasource.RouteDataSource
import data.mapper.RouteMapper
import domain.model.Route
import domain.model.updateinput.RouteUpdateModel
import domain.repository.RouteRepository
import domain.repository.WarehouseRepository

class LocalRouteRepositoryImpl(
    private val routeDataSource: RouteDataSource,
    private val routeMapper: RouteMapper,
    private val warehouseRepository: WarehouseRepository
) : RouteRepository {

    private val routes = mutableListOf<Route>()
    private var isLoaded = false

    private suspend fun loadRoutesIfNeeded() {
        if (isLoaded) return

        val rawRoutes = routeDataSource.loadRoutes()

        rawRoutes.forEach { rawRoute ->
            val originWarehouse =
                warehouseRepository.findById(rawRoute.originHubId)

            val destinationWarehouse =
                warehouseRepository.findById(rawRoute.destinationHubId)

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

    override suspend fun findById(routeId: String): Route? {
        loadRoutesIfNeeded()
        return routes.find { it.id == routeId }
    }

    override suspend fun findAll(): List<Route> {
        loadRoutesIfNeeded()
        return routes.toList()
    }

    override suspend fun add(route: Route): Route {
        loadRoutesIfNeeded()

        routes.add(route)
        route.originWarehouse.addRoute(route)

        return route
    }

    override suspend fun update(
        routeId: String,
        updateModel: RouteUpdateModel
    ): Route {
        loadRoutesIfNeeded()

        val currentRoute = routes.find { it.id == routeId }
            ?: throw IllegalStateException("Route not found: $routeId")

        val newOriginWarehouse =
            updateModel.originWarehouseId
                ?.let { warehouseRepository.findById(it) }
                ?: currentRoute.originWarehouse

        val newDestinationWarehouse =
            updateModel.destinationWarehouseId
                ?.let { warehouseRepository.findById(it) }
                ?: currentRoute.destinationWarehouse

        val updatedRoute = currentRoute.copy(
            originWarehouse = newOriginWarehouse,
            destinationWarehouse = newDestinationWarehouse,
            distanceKm = updateModel.distanceKm
                ?: currentRoute.distanceKm,
            typicalDelayMin = updateModel.typicalDelayMin
                ?: currentRoute.typicalDelayMin
        )

        updateRouteRelations(
            previousRoute = currentRoute,
            updatedRoute = updatedRoute
        )

        val routeIndex = routes.indexOfFirst { it.id == routeId }
        routes[routeIndex] = updatedRoute

        return updatedRoute
    }

    private fun updateRouteRelations(
        previousRoute: Route,
        updatedRoute: Route
    ) {
        val previousOriginWarehouse = previousRoute.originWarehouse
        val newOriginWarehouse = updatedRoute.originWarehouse

        if (previousOriginWarehouse.id == newOriginWarehouse.id) {
            val routeIndex =
                previousOriginWarehouse.outgoingRoutes
                    .indexOfFirst { it.id == previousRoute.id }

            if (routeIndex != -1) {
                previousOriginWarehouse.updateRouteAtIndex(
                    routeIndex,
                    updatedRoute
                )
            }

            return
        }

        previousOriginWarehouse.removeRoute(previousRoute)
        newOriginWarehouse.addRoute(updatedRoute)
    }

    override suspend fun delete(routeId: String) {
        loadRoutesIfNeeded()

        val route = routes.find { it.id == routeId } ?: return

        route.originWarehouse.removeRoute(route)
        routes.remove(route)
    }
}