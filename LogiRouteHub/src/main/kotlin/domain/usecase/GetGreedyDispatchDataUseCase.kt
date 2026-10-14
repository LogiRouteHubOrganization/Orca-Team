package domain.usecase

import domain.algorithm.greedy.VehicleCoverage
import domain.algorithm.greedy.VehicleCoverageMapper
import domain.extension.runCatchingCancellable
import domain.repository.RouteRepository
import domain.repository.VehicleRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async


class GetGreedyDispatchDataUseCase(
    private val vehicleRepository: VehicleRepository,
    private val routeRepository: RouteRepository,
    private val greedyInputPreparer: VehicleCoverageMapper
) {

    suspend operator fun invoke(): Result<List<VehicleCoverage>> = runCatchingCancellable {

        val (vehicles, routes) = coroutineScope {
            val vehiclesDeferred = async { vehicleRepository.findAll().getOrThrow() }

            val routesDeferred = async { routeRepository.findAll().getOrThrow() }

            Pair(vehiclesDeferred.await(), routesDeferred.await())
        }

        greedyInputPreparer.prepareVehicleCoverage(vehicles, routes)
    }
}
