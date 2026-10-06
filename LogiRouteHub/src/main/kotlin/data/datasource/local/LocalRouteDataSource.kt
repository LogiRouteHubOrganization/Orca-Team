package data.datasource.local

import data.dataholder.RouteRaw

interface LocalRouteDataSource {

    fun loadRoutes(): List<RouteRaw>

    fun addRoute(route: RouteRaw)

    fun updateRoute(route: RouteRaw)

    fun deleteRoute(routeId: String)

    fun replaceRoutes(routes: List<RouteRaw>)
}
