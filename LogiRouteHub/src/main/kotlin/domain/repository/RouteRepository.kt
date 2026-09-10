package domain.repository

import domain.model.Route

interface RouteRepository {

    fun findById(routeId: String): Route?

    fun findAll(): List<Route>

    fun add(route: Route)

    fun update(route: Route)
}
