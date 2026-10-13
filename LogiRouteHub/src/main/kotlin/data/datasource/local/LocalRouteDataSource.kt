
package data.datasource.local

import data.dataholder.RouteRaw

interface LocalRouteDataSource {

    suspend fun loadRoutes(): List<RouteRaw>

    suspend fun addRoute(route: RouteRaw)

    suspend fun updateRoute(route: RouteRaw)

    suspend fun deleteRoute(routeId: String)

    suspend fun replaceRoutes(routes: List<RouteRaw>)
}
