package domain.usecase

import domain.algorithm.greedy.VehicleCoverage
import domain.algorithm.greedy.VehicleCoverageMapper
import domain.extension.runCatchingCancellable
import domain.repository.RouteRepository
import domain.repository.VehicleRepository

class GetGreedyDispatchDataUseCase(
    private val vehicleRepository: VehicleRepository,
    private val routeRepository: RouteRepository,
    private val greedyInputPreparer: VehicleCoverageMapper
) {

    suspend operator fun invoke(): Result<List<VehicleCoverage>> = runCatchingCancellable {

        val vehicles = vehicleRepository.findAll().getOrThrow()
        val routes = routeRepository.findAll().getOrThrow()


        greedyInputPreparer.prepareVehicleCoverage(vehicles, routes)
    }
}
