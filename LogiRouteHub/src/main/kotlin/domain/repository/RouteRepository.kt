
package domain.repository

import domain.model.Route
import domain.model.updateinput.RouteUpdateModel

interface RouteRepository {

    suspend fun findById(routeId: String): Result<Route?>

    suspend fun findAll(): Result<List<Route>>

    suspend fun add(route: Route): Result<Route>

    suspend fun update(
        routeId: String,
        updateModel: RouteUpdateModel
    ): Result<Route>

    suspend fun delete(routeId: String): Result<Unit>
}

