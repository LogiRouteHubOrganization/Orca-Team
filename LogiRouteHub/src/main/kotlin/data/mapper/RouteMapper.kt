package data.mapper

import data.dataholder.RouteRaw
import domain.model.Route
import domain.model.Warehouse

class RouteMapper {

    fun mapToDomainModel(
        rawRoute: RouteRaw,
        originWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): Route {
        return Route(
            id = rawRoute.routeId,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse,
            distanceKm = rawRoute.distanceKm,
            typicalDelayMin = rawRoute.typicalDelayMin
        )
    }
}
