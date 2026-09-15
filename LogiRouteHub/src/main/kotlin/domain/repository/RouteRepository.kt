package domain.repository

import domain.model.Route
import domain.model.updateinput.RouteUpdateModel

interface RouteRepository {

    suspend fun findById(routeId: String): Route?

    suspend fun findAll(): List<Route>

    suspend fun add(route: Route): Route

    suspend fun update( routeId: String, updateModel: RouteUpdateModel): Route

    suspend fun delete(routeId: String)
}
