package domain.usecase.crud.route

import domain.model.Route
import domain.repository.RouteRepository

class CreateRouteUseCase(
    private val routeRepository: RouteRepository
) {

    suspend operator fun invoke(
        route: Route
    ): Result<Route> {
        return routeRepository.add(route)
    }
}
