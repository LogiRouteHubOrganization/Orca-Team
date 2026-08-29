package domain.usecase

import domain.model.Package
import domain.model.Route
import domain.model.Warehouse
import domain.routing.ShortestPathRouter

data class PackageJourney(
    val packageId: String,
    val originWarehouse: Warehouse,
    val destinationWarehouse: Warehouse,
    val warehouses: List<Warehouse>,
    val routes: List<Route>,
    val totalDistanceKm: Double,
    val totalTypicalDelayMin: Int
)

class GetPackageJourneyUseCase(
    private val shortestPathRouter: ShortestPathRouter
) {

    operator fun invoke(
        logisticsPackage: Package
    ): PackageJourney {

        val warehousePath = shortestPathRouter.findShortestPath(
            startWarehouse = logisticsPackage.originWarehouse,
            destinationWarehouse = logisticsPackage.destinationWarehouse
        )

        val routePath = warehousePath
            .zipWithNext()
            .mapNotNull { (origin, destination) ->
                origin.outgoingRoutes.firstOrNull { route ->
                    route.destinationWarehouse.id == destination.id
                }
            }

        return PackageJourney(
            packageId = logisticsPackage.id,
            originWarehouse = logisticsPackage.originWarehouse,
            destinationWarehouse = logisticsPackage.destinationWarehouse,
            warehouses = warehousePath,
            routes = routePath,
            totalDistanceKm = routePath.sumOf { it.distanceKm },
            totalTypicalDelayMin = routePath.sumOf { it.typicalDelayMin }
        )
    }
}