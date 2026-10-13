package domain.usecase

import domain.extension.runCatchingCancellable
import domain.model.request.PackageJourney
import domain.repository.PackageRepository

class GetPackageJourneyUseCase(
    private val packageRepository: PackageRepository,
    private val findOptimalPathUseCase: FindOptimalPathUseCase
) {

    suspend operator fun invoke(
        packageId: String
    ):  Result<PackageJourney?> = runCatchingCancellable {

        val logisticsPackage = packageRepository.findById(packageId).getOrThrow()
            ?: return@runCatchingCancellable null

        val warehousePath = findOptimalPathUseCase(
            startWarehouseId = logisticsPackage.originWarehouse.id,
            destinationWarehouseId = logisticsPackage.destinationWarehouse.id
        ).getOrThrow()

        val routePath = warehousePath
            .zipWithNext()
            .mapNotNull { (origin, destination) ->
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
