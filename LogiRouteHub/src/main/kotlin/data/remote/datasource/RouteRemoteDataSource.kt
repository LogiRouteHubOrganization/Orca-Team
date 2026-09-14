package data.remote.datasource

import data.remote.dto.RouteDto
import data.remote.dto.request.RouteRequestDto

interface RouteRemoteDataSource {
    suspend fun getAllRoutes(): List<RouteDto>
    suspend fun getRouteById(id: String): RouteDto?
    suspend fun createRoute(routeRequest: RouteRequestDto): RouteDto
    suspend fun updateRoute(id: String, routeRequest: RouteRequestDto): RouteDto
    suspend fun deleteRoute(id: String)
}
