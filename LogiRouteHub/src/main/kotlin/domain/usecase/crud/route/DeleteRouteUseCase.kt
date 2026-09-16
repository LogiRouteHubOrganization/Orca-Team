package domain.usecase.crud.route

import domain.repository.RouteRepository

class DeleteRouteUseCase (
    private val routeRepository: RouteRepository
) {
    suspend operator fun invoke(routeId: String) {
        routeRepository.delete(routeId)
    }
}