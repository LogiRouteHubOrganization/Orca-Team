package domain.repository

import domain.model.Route

interface RouteRepository {

    suspend fun findById(routeId: String): Route?

    suspend fun findAll(): List<Route>

    suspend fun add(route: Route)

    suspend fun update(route: Route)

    suspend fun delete(routeId: String)
}
