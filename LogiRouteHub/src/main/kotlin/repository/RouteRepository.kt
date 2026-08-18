package repository

import dataholder.RouteRaw

interface RouteRepository {
    fun loadRoutes(): List<RouteRaw>
}
