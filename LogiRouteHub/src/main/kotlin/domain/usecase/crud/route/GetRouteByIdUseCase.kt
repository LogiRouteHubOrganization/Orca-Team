package domain.usecase.crud.route

import domain.exception.EntityValidationException
import domain.model.Route
import domain.repository.RouteRepository
import domain.validator.ResultVaildator
import domain.validator.rule.IdValidator

class GetRouteByIdUseCase(
    private val routeRepository: RouteRepository
) {
    suspend operator fun invoke(routeId: String): Result<Route?> {

        return when (
            val validationResult = IdValidator(
                field = "routeId",
                allowedPrefixes = setOf("RT-")
            ).validate(routeId)
        ) {
            ResultVaildator.Valid ->
                routeRepository.findById(routeId)

            is ResultVaildator.Invalid ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}
