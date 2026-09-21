package domain.usecase

import domain.algorithm.dispatcher.VehicleCoverage
import domain.algorithm.dispatcher.VehicleCoverageMapper
import domain.repository.RouteRepository
import domain.repository.VehicleRepository

class GetGreedyDispatchDataUseCase(
    private val vehicleRepository: VehicleRepository,
    private val routeRepository: RouteRepository,
    private val greedyInputPreparer: VehicleCoverageMapper
) {

    suspend operator fun invoke(): Result<List<VehicleCoverage>> = runCatching {

        val vehicles = vehicleRepository.findAll().getOrThrow()
        val routes = routeRepository.findAll().getOrThrow()


        greedyInputPreparer.prepareVehicleCoverage(vehicles, routes)
    }
}
