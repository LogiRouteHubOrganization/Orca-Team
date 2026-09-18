package data.repository.remote

import data.remote.datasource.RouteRemoteDataSource
import data.remote.exception.toDomainException
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

    override suspend fun findById(routeId: String): Route? {
        return try {
            val routeDto = routeRemoteDataSource.getRouteById(routeId)
                ?: return null

            val originWarehouse = warehouseRepository.findById(routeDto.originWarehouseId)
                ?: throw ResourceNotFoundException("Warehouse", routeDto.originWarehouseId)

            val destinationWarehouse = warehouseRepository.findById(routeDto.destinationWarehouseId)
                ?: throw ResourceNotFoundException("Warehouse", routeDto.destinationWarehouseId)

            routeDto.toDomain(
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Route", resourceId = routeId)
        }
    }

    override suspend fun findAll(): List<Route> {
        return try {
            val routeDtos = routeRemoteDataSource.getAllRoutes()
            val warehouses = warehouseRepository.findAll()
            val warehousesById = warehouses.associateBy { it.id }

            routeDtos.map { routeDto ->
                val originWarehouse = warehousesById[routeDto.originWarehouseId]
                    ?: throw ResourceNotFoundException("Warehouse", routeDto.originWarehouseId)

                val destinationWarehouse = warehousesById[routeDto.destinationWarehouseId]
                    ?: throw ResourceNotFoundException("Warehouse", routeDto.destinationWarehouseId)

                routeDto.toDomain(
                    originWarehouse = originWarehouse,
                    destinationWarehouse = destinationWarehouse
                )
            }
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Route")
        }
    }

    override suspend fun add(route: Route): Route {
        return try {
            val createdDto = routeRemoteDataSource.createRoute(
                route.toCreateDto()
            )

            val originWarehouse = warehouseRepository.findById(createdDto.originWarehouseId)
                ?: throw ResourceNotFoundException("Warehouse", createdDto.originWarehouseId)

            val destinationWarehouse = warehouseRepository.findById(createdDto.destinationWarehouseId)
                ?: throw ResourceNotFoundException("Warehouse", createdDto.destinationWarehouseId)

            createdDto.toDomain(
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Route", resourceId = route.id)
        }
    }

    override suspend fun update(
        routeId: String,
        updateModel: RouteUpdateModel
    ): Route {
        return try {
            val updatedDto = routeRemoteDataSource.updateRoute(
                id = routeId,
                routeRequest = updateModel.toUpdateDto()
            )

            val originWarehouse = warehouseRepository.findById(updatedDto.originWarehouseId)
                ?: throw ResourceNotFoundException("Warehouse", updatedDto.originWarehouseId)

            val destinationWarehouse = warehouseRepository.findById(updatedDto.destinationWarehouseId)
                ?: throw ResourceNotFoundException("Warehouse", updatedDto.destinationWarehouseId)

            updatedDto.toDomain(
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Route", resourceId = routeId)
        }
    }

    override suspend fun delete(routeId: String) {
        try {
            routeRemoteDataSource.deleteRoute(routeId)
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Route", resourceId = routeId)
        }
    }
}
