
package data.repository.remote

import data.remote.datasource.RouteRemoteDataSource
import data.exception.remote.toDomainException
import data.remote.mapper.toCreateDto
import data.remote.mapper.toDomain
import data.remote.mapper.toUpdateDto
import domain.exception.ResourceNotFoundException
import domain.model.Route
import domain.model.updateinput.RouteUpdateModel
import domain.repository.RouteRepository
import domain.repository.WarehouseRepository

class RemoteRouteRepositoryImpl(
    private val routeRemoteDataSource: RouteRemoteDataSource,
    private val warehouseRepository: WarehouseRepository
) : RouteRepository {

    override suspend fun findById(
        routeId: String
    ): Result<Route?> {

        return runCatching {
            val routeDto = routeRemoteDataSource.getRouteById(routeId)
                ?: return@runCatching null

            val originWarehouse = warehouseRepository
                .findById(routeDto.originWarehouseId)
                .getOrThrow()
                ?: throw ResourceNotFoundException(
                    "Warehouse",
                    routeDto.originWarehouseId
                )

            val destinationWarehouse = warehouseRepository
                .findById(routeDto.destinationWarehouseId)
                .getOrThrow()
                ?: throw ResourceNotFoundException(
                    "Warehouse",
                    routeDto.destinationWarehouseId
                )

            routeDto.toDomain(
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Route",
                resourceId = routeId
            )
        }
    }

    override suspend fun findAll(): Result<List<Route>> {

        return runCatching {
            val routeDtos = routeRemoteDataSource.getAllRoutes()

            val warehouses = warehouseRepository
                .findAll()
                .getOrThrow()

            val warehousesById = warehouses.associateBy { it.id }

            routeDtos.map { routeDto ->

                val originWarehouse = warehousesById[
                    routeDto.originWarehouseId
                ] ?: throw ResourceNotFoundException(
                    "Warehouse",
                    routeDto.originWarehouseId
                )

                val destinationWarehouse = warehousesById[
                    routeDto.destinationWarehouseId
                ] ?: throw ResourceNotFoundException(
                    "Warehouse",
                    routeDto.destinationWarehouseId
                )

                routeDto.toDomain(
                    originWarehouse = originWarehouse,
                    destinationWarehouse = destinationWarehouse
                )
            }
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Route"
            )
        }
    }

    override suspend fun add(
        route: Route
    ): Result<Route> {

        return runCatching {
            val createdDto = routeRemoteDataSource.createRoute(
                route.toCreateDto()
            )

            val originWarehouse = warehouseRepository
                .findById(createdDto.originWarehouseId)
                .getOrThrow()
                ?: throw ResourceNotFoundException(
                    "Warehouse",
                    createdDto.originWarehouseId
                )

            val destinationWarehouse = warehouseRepository
                .findById(createdDto.destinationWarehouseId)
                .getOrThrow()
                ?: throw ResourceNotFoundException(
                    "Warehouse",
                    createdDto.destinationWarehouseId
                )

            createdDto.toDomain(
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Route",
                resourceId = route.id
            )
        }
    }

    override suspend fun update(
        routeId: String,
        updateModel: RouteUpdateModel
    ): Result<Route> {

        return runCatching {
            val updatedDto = routeRemoteDataSource.updateRoute(
                id = routeId,
                routeRequest = updateModel.toUpdateDto()
            )

            val originWarehouse = warehouseRepository
                .findById(updatedDto.originWarehouseId)
                .getOrThrow()
                ?: throw ResourceNotFoundException(
                    "Warehouse",
                    updatedDto.originWarehouseId
                )

            val destinationWarehouse = warehouseRepository
                .findById(updatedDto.destinationWarehouseId)
                .getOrThrow()
                ?: throw ResourceNotFoundException(
                    "Warehouse",
                    updatedDto.destinationWarehouseId
                )

            updatedDto.toDomain(
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Route",
                resourceId = routeId
            )
        }
    }

    override suspend fun delete(
        routeId: String
    ): Result<Unit> {

        return runCatching {
            routeRemoteDataSource.deleteRoute(routeId)
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Route",
                resourceId = routeId
            )
        }
    }
}
