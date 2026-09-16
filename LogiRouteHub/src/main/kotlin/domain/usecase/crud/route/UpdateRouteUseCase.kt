package domain.usecase.crud.route

import domain.model.Route
import domain.model.updateinput.RouteUpdateModel
import domain.repository.RouteRepository

class UpdateRouteUseCase (
    private val routeRepository: RouteRepository
) {
    suspend operator fun invoke(
        routeId: String,
        updateModel: RouteUpdateModel
    ): Route {
        return routeRepository.update(
            routeId = routeId,
            updateModel = updateModel
        )
    }
}