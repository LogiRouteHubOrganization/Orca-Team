package data.repository.memory

import domain.model.Route
import domain.repository.RouteRepository

class InMemoryRouteRepository(
    private val routes: MutableList<Route> = mutableListOf()
) : RouteRepository {

    override fun findById(routeId: String): Route? {
        return routes.find { it.id == routeId }
    }

    override fun findAll(): List<Route> {
        return routes
    }

    override fun add(route: Route) {
        routes.add(route)
    }

    override fun update(route: Route) {

        val notFoundIndex = -1
        val routeIndex = routes.indexOfFirst {
            it.id == route.id
        }

        if (routeIndex != notFoundIndex) {
            routes[routeIndex] = route
        }
    }
}
