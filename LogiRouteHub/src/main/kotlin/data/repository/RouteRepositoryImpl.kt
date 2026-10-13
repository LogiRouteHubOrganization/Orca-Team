package data.repository

import data.datasource.local.LocalRouteDataSource
import data.exception.local.LocalDataException
import data.exception.local.toDomainException
import data.exception.remote.toDomainException
import data.mapper.RouteMapper
import data.datasource.remote.RemoteRouteDataSource
import data.remote.mapper.toCreateDto
import data.remote.mapper.toRaw
import data.remote.mapper.toUpdateDto
import data.extension.runCatchingCancellable
import data.extension.recoverCatchingCancellable
import domain.exception.ResourceNotFoundException
import domain.model.Route
import domain.model.updateinput.RouteUpdateModel
import domain.repository.RouteRepository
import domain.repository.WarehouseRepository

class RouteRepositoryImpl(
    private val routeDataSource: LocalRouteDataSource,
    private val routeRemoteDataSource: RemoteRouteDataSource,
    private val routeMapper: RouteMapper,
    private val warehouseRepository: WarehouseRepository
) : RouteRepository {

    private val routes = mutableListOf<Route>()
    private var isLoaded = false

    override suspend fun findById(
        routeId: String
    ): Result<Route?> =
        runCatchingCancellable {
            loadLocalRoutes()

            routes.find { route ->
                route.id == routeId
            }
        }.mapLocalDataException()

    override suspend fun findAll(): Result<List<Route>> =
        runCatchingCancellable {
            loadLocalRoutes()

            routes.toList()
        }.mapLocalDataException()

    override suspend fun add(
        route: Route
    ): Result<Route> =
        runCatchingCancellable {
            loadLocalRoutes()

            val createdDto = routeRemoteDataSource.createRoute(
                route.toCreateDto()
            )

            val createdRaw = createdDto.toRaw()

            routeDataSource.addRoute(createdRaw)

            val originWarehouse = warehouseRepository
                .findById(createdRaw.originHubId)
                .getOrThrow()
                ?: throw ResourceNotFoundException.WarehouseNotFound(
                    createdRaw.originHubId
                )

            val destinationWarehouse = warehouseRepository
                .findById(createdRaw.destinationHubId)
                .getOrThrow()
                ?: throw ResourceNotFoundException.WarehouseNotFound(
                    createdRaw.destinationHubId
                )

            val createdRoute = routeMapper.mapToDomainModel(
                rawRoute = createdRaw,
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )

            routes.add(createdRoute)
            originWarehouse.addRoute(createdRoute)

            createdRoute
        }.mapRepositoryException(
            resourceId = route.id
        )

    override suspend fun update(
        routeId: String,
        updateModel: RouteUpdateModel
    ): Result<Route> = runCatchingCancellable {
        loadLocalRoutes()

        val previousRoute = routes.find { it.id == routeId }
            ?: throw ResourceNotFoundException.RouteNotFound(routeId)

        val updatedRaw = routeRemoteDataSource.updateRoute(
            id = routeId,
            routeRequest = updateModel.toUpdateDto()
        ).toRaw()

        routeDataSource.updateRoute(updatedRaw)

        val originWarehouse = warehouseRepository.findById(updatedRaw.originHubId).getOrThrow()
            ?: throw ResourceNotFoundException.WarehouseNotFound(updatedRaw.originHubId)

        val destinationWarehouse = warehouseRepository.findById(updatedRaw.destinationHubId).getOrThrow()
            ?: throw ResourceNotFoundException.WarehouseNotFound(updatedRaw.destinationHubId)

        val updatedRoute = routeMapper.mapToDomainModel(
            rawRoute = updatedRaw,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        updateRouteRelations(previousRoute = previousRoute, updatedRoute = updatedRoute)

        routes.replaceAll { if (it.id == routeId) updatedRoute else it }
        updatedRoute
    }.mapRepositoryException(resourceId = routeId)

    override suspend fun delete(
        routeId: String
    ): Result<Unit> = runCatchingCancellable {
        loadLocalRoutes()

        val route = routes.find { currentRoute ->
            currentRoute.id == routeId
        } ?: throw ResourceNotFoundException.RouteNotFound(
            routeId
        )

        routeRemoteDataSource.deleteRoute(routeId)

        routeDataSource.deleteRoute(routeId)

        route.originWarehouse.removeRoute(route)
        routes.remove(route)

        Unit
    }.mapRepositoryException(
        resourceId = routeId
    )

    override suspend fun refresh(): Result<Unit> =
        runCatchingCancellable {
            val remoteRoutes = routeRemoteDataSource.getAllRoutes()

            val localRoutes = remoteRoutes.map { routeDto ->
                routeDto.toRaw()
            }

            routeDataSource.replaceRoutes(localRoutes)
        }.recoverCatchingCancellable { exception ->
            throw exception.toDomainException(
                resourceType = "Route"
            )
        }

    override suspend fun initializeOutgoingRoutes(): Result<Unit> =
        runCatchingCancellable {
            loadLocalRoutes()
        }.mapLocalDataException()

    private suspend fun loadLocalRoutes() {
        if (isLoaded) {
            return
        }

        val rawRoutes = routeDataSource.loadRoutes()

        rawRoutes.forEach { rawRoute ->

            val originWarehouse = warehouseRepository
                .findById(rawRoute.originHubId)
                .getOrThrow()

            if (originWarehouse == null) {
                println(
                    "Warning: Skipping route '${rawRoute.routeId}' " +
                            "because origin warehouse '${rawRoute.originHubId}' " +
                            "was not found"
                )
                return@forEach
            }

            val destinationWarehouse = warehouseRepository
                .findById(rawRoute.destinationHubId)
                .getOrThrow()

            if (destinationWarehouse == null) {
                println(
                    "Warning: Skipping route '${rawRoute.routeId}' " +
                            "because destination warehouse " +
                            "'${rawRoute.destinationHubId}' was not found"
                )
                return@forEach
            }

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
                    .indexOfFirst { route ->
                        route.id == previousRoute.id
                    }

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

    private fun <T> Result<T>.mapLocalDataException(): Result<T> {
        return recoverCatchingCancellable { throwable ->
            throw when (throwable) {
                is LocalDataException ->
                    throwable.toDomainException()

                else -> throwable
            }
        }
    }

    private fun <T> Result<T>.mapRepositoryException(
        resourceId: String
    ): Result<T> {
        return recoverCatchingCancellable { exception ->
            throw when (exception) {
                is LocalDataException -> exception.toDomainException()

                else ->
                    exception.toDomainException(
                        resourceType = "Route",
                        resourceId = resourceId
                    )
            }
        }
    }
}
