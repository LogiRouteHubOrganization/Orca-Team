package data.remote.datasource

import data.remote.dto.RouteDto
import data.remote.dto.request.RouteCreateRequestDto
import data.remote.dto.request.RouteUpdateRequestDto

interface RouteRemoteDataSource {

    suspend fun getAllRoutes(): List<RouteDto>

    suspend fun getRouteById(id: String): RouteDto?

    suspend fun createRoute(
        routeRequest: RouteCreateRequestDto
    ): RouteDto

    suspend fun updateRoute(
        id: String,
        routeRequest: RouteUpdateRequestDto
    ): RouteDto

    suspend fun deleteRoute(id: String)
}
