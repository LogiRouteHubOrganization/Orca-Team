package domain.usecase.crud.route

import domain.exception.EntityValidationException
import domain.model.Route
import domain.model.updateinput.RouteUpdateModel
import domain.repository.RouteRepository
import domain.validator.ResultVaildator
import domain.validator.route.UpdateRouteValidator

class UpdateRouteUseCase(
    private val routeRepository: RouteRepository,
    private val routeValidator: UpdateRouteValidator
) {

    suspend operator fun invoke(
        routeId: String,
        updateModel: RouteUpdateModel
    ): Result<Route> {

        return when (
            val validationResult = routeValidator(
                routeId = routeId,
                updateModel = updateModel
            )
        ) {
            ResultVaildator.Valid -> routeRepository.update(
                    routeId = routeId,
                    updateModel = updateModel
                )

            is ResultVaildator.Invalid -> Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}

