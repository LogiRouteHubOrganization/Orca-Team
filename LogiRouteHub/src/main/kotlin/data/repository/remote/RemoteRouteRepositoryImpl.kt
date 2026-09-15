package data.repository.remote

import domain.repository.RouteRepository
import domain.repository.WarehouseRepository
import data.remote.datasource.RouteRemoteDataSource
import data.remote.mapper.toDomain
import domain.model.Route
import domain.model.updateinput.RouteUpdateModel
import data.remote.dto.request.RouteUpdateRequestDto
import data.remote.mapper.toCreateDto
import data.remote.mapper.toUpdateDto

class RemoteRouteRepositoryImpl (
    private val routeRemoteDataSource: RouteRemoteDataSource,
    private val warehouseRepository: WarehouseRepository
) : RouteRepository {
    override suspend fun findById(routeId: String): Route? {
        val routeDto =
            routeRemoteDataSource.getRouteById(routeId)
                ?: return null

        val originWarehouse =
            warehouseRepository.findById(routeDto.originWarehouseId)
                ?: throw IllegalStateException(
                    "Origin warehouse not found: ${routeDto.originWarehouseId}"
                )

        val destinationWarehouse =
            warehouseRepository.findById(routeDto.destinationWarehouseId)
                ?: throw IllegalStateException(
                    "Destination warehouse not found: ${routeDto.destinationWarehouseId}"
                )

        return routeDto.toDomain(
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }

    override suspend fun findAll(): List<Route> {
        val routeDtos =
            routeRemoteDataSource.getAllRoutes()

        val warehouses =
            warehouseRepository.findAll()

        val warehousesById =
            warehouses.associateBy { it.id }

        return routeDtos.map { routeDto ->

            val originWarehouse =
                warehousesById[routeDto.originWarehouseId]
                    ?: throw IllegalStateException(
                        "Origin warehouse not found: ${routeDto.originWarehouseId}"
                    )

            val destinationWarehouse =
                warehousesById[routeDto.destinationWarehouseId]
                    ?: throw IllegalStateException(
                        "Destination warehouse not found: ${routeDto.destinationWarehouseId}"
                    )

            routeDto.toDomain(
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }
    }

    override suspend fun add(route: Route): Route {
        val createdDto =
            routeRemoteDataSource.createRoute(
                route.toCreateDto()
            )

        val originWarehouse =
            warehouseRepository.findById(createdDto.originWarehouseId)
                ?: throw IllegalStateException(
                    "Origin warehouse not found: ${createdDto.originWarehouseId}"
                )

        val destinationWarehouse =
            warehouseRepository.findById(createdDto.destinationWarehouseId)
                ?: throw IllegalStateException(
                    "Destination warehouse not found: ${createdDto.destinationWarehouseId}"
                )

        return createdDto.toDomain(
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }

    override suspend fun update(
        routeId: String,
        updateModel: RouteUpdateModel
    ): Route {
        val updatedDto =
            routeRemoteDataSource.updateRoute(
                id = routeId,
                routeRequest = updateModel.toUpdateDto()
            )

        val originWarehouse =
            warehouseRepository.findById(updatedDto.originWarehouseId)
                ?: throw IllegalStateException(
                    "Origin warehouse not found: ${updatedDto.originWarehouseId}"
                )

        val destinationWarehouse =
            warehouseRepository.findById(updatedDto.destinationWarehouseId)
                ?: throw IllegalStateException(
                    "Destination warehouse not found: ${updatedDto.destinationWarehouseId}"
                )

        return updatedDto.toDomain(
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }

    override suspend fun delete(routeId: String) {
        routeRemoteDataSource.deleteRoute(routeId)
    }
}