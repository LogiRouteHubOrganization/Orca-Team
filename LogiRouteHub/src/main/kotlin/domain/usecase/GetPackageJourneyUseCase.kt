package domain.usecase

import domain.model.Route
import domain.model.Warehouse
import domain.model.request.PackageJourney
import domain.repository.PackageRepository
import domain.routing.ShortestPathRouter


class GetPackageJourneyUseCase(
    private val packageRepository: PackageRepository,
    private val shortestPathRouter: ShortestPathRouter
) {

    suspend operator fun invoke(
        packageId: String
    ): Result<PackageJourney?> = runCatching {

        val logisticsPackage = packageRepository.findById(packageId).getOrThrow()
            ?: return@runCatching null

        val warehousePath = shortestPathRouter.findShortestPath(
            startWarehouse = logisticsPackage.originWarehouse,
            destinationWarehouse = logisticsPackage.destinationWarehouse
        )

        val routePath = warehousePath.zipWithNext().mapNotNull { (origin, destination) ->
                origin.outgoingRoutes.firstOrNull { route ->
                    route.destinationWarehouse.id == destination.id
                }
            }

        PackageJourney(
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
