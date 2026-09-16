package domain.usecase.crud.route

import domain.model.Route
import domain.repository.RouteRepository


class GetRouteByIdUseCase (
    private val routeRepository: RouteRepository
) {
    suspend operator fun invoke(routeId: String): Route? {
        return routeRepository.findById(routeId)
    }
}