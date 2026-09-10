package data.datasource

import data.dataholder.RouteRaw

interface RouteDataSource {
    fun loadRoutes(): List<RouteRaw>
}
