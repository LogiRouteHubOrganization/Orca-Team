package data.repository.local

import data.datasource.RouteDataSource
import data.exception.local.LocalDataException
import data.exception.local.toDomainException
import data.mapper.RouteMapper
import domain.exception.ResourceNotFoundException
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
                warehouseRepository
                    .findById(rawRoute.originHubId)
                    .getOrThrow()
                    ?: throw ResourceNotFoundException.WarehouseNotFound(rawRoute.originHubId)

            val destinationWarehouse =
                warehouseRepository
                    .findById(rawRoute.destinationHubId)
                    .getOrThrow()
                    ?: throw ResourceNotFoundException.WarehouseNotFound(rawRoute.destinationHubId)

            val route = routeMapper.mapToDomainModel(
                rawRoute = rawRoute,
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )

            routes.add(route)
            originWarehouse.addRoute(route)
        }

        isLoaded = true
    }

    override suspend fun findById(
        routeId: String
    ): Result<Route?> =
        runCatching {
            loadRoutesIfNeeded()

            routes.find { it.id == routeId }
        }.mapLocalDataException()

    override suspend fun findAll(): Result<List<Route>> =
        runCatching {
            loadRoutesIfNeeded()

            routes.toList()
        }.mapLocalDataException()

    override suspend fun add(
        route: Route
    ): Result<Route> =
        runCatching {
            loadRoutesIfNeeded()

            routes.add(route)
            route.originWarehouse.addRoute(route)

            route
        }.mapLocalDataException()

    override suspend fun update(
        routeId: String,
        updateModel: RouteUpdateModel
    ): Result<Route> =
        runCatching {
            loadRoutesIfNeeded()

            val currentRoute =
                routes.find { it.id == routeId }
                    ?: throw ResourceNotFoundException.RouteNotFound(routeId)

            val newOriginWarehouse =
                updateModel.originWarehouseId
                    ?.let { warehouseId ->
                        warehouseRepository
                            .findById(warehouseId)
                            .getOrThrow()
                            ?: throw ResourceNotFoundException.WarehouseNotFound(warehouseId)
                    }
                    ?: currentRoute.originWarehouse

            val newDestinationWarehouse =
                updateModel.destinationWarehouseId
                    ?.let { warehouseId ->
                        warehouseRepository
                            .findById(warehouseId)
                            .getOrThrow()
                            ?: throw ResourceNotFoundException.WarehouseNotFound(warehouseId)
                    }
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

            val routeIndex =
                routes.indexOfFirst { it.id == routeId }

            if (routeIndex != -1) {
                routes[routeIndex] = updatedRoute
            }

            updatedRoute
        }.mapLocalDataException()

    private fun updateRouteRelations(
        previousRoute: Route,
        updatedRoute: Route
    ) {
        val previousOriginWarehouse =
            previousRoute.originWarehouse

        val newOriginWarehouse =
            updatedRoute.originWarehouse

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

    override suspend fun delete(
        routeId: String
    ): Result<Unit> =
        runCatching {
            loadRoutesIfNeeded()

            val route =
                routes.find { it.id == routeId }
                    ?: throw ResourceNotFoundException.RouteNotFound(routeId)

            route.originWarehouse.removeRoute(route)
            routes.remove(route)
            Unit
        }.mapLocalDataException()


    private fun <T> Result<T>.mapLocalDataException(): Result<T> {
        return recoverCatching { throwable ->
            throw when (throwable) {
                is LocalDataException -> throwable.toDomainException()
                else -> throwable
            }
        }
    }
}
