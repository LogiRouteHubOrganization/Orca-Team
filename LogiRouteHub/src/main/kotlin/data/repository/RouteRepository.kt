package data.repository

import data.dataholder.RouteRaw

interface RouteRepository {
    fun loadRoutes(): List<RouteRaw>
}
