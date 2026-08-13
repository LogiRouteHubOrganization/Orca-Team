package repository

import dataHolder.RouteRaw


interface RouteRepository {

    fun loadRoutes(): List<RouteRaw>
}