package data.remote.repository

import data.remote.datasource.RouteRemoteDataSource
import data.remote.mapper.toDomain
import data.remote.mapper.toRequestDto
import data.remote.utils.toDomainException
import domain.model.Route
import domain.repository.RouteRepository

class RouteRemoteRepositoryImpl(
    private val remoteDataSource: RouteRemoteDataSource,
    private val warehouseRepository: domain.repository.WarehouseRepository
) : RouteRepository {

    override suspend fun findAll(): List<Route> {
        return try {
            remoteDataSource.getAllRoutes().mapNotNull { dto ->
                val originWarehouse =
                    warehouseRepository.findById(dto.originWarehouseId)

                val destinationWarehouse =
                    warehouseRepository.findById(dto.destinationWarehouseId)

                if (originWarehouse == null || destinationWarehouse == null) {
                    null
                } else {
                    dto.toDomain(
                        originWarehouse = originWarehouse,
                        destinationWarehouse = destinationWarehouse
                    )
                }
            }
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }

    override suspend fun findById(routeId: String): Route? {
        return try {
            val dto = remoteDataSource.getRouteById(routeId)
                ?: return null

            val originWarehouse =
                warehouseRepository.findById(dto.originWarehouseId)
                    ?: return null

            val destinationWarehouse =
                warehouseRepository.findById(dto.destinationWarehouseId)
                    ?: return null

            dto.toDomain(
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }

    override suspend fun add(route: Route) {
        try {
            remoteDataSource.createRoute(
                route.toRequestDto()
            )
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }

    override suspend fun update(route: Route) {
        try {
            remoteDataSource.updateRoute(
                route.id,
                route.toRequestDto()
            )
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }

    override suspend fun delete(routeId: String) {
        try {
            remoteDataSource.deleteRoute(routeId)
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }
}