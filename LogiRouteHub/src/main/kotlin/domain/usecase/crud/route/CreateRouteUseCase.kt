package domain.usecase.crud.route

import domain.exception.EntityValidationException
import domain.model.Route
import domain.repository.RouteRepository
import domain.validator.ValidationResult
import domain.validator.route.CreateRouteValidator

class CreateRouteUseCase(
    private val routeRepository: RouteRepository,
    private val routeValidator: CreateRouteValidator
) {
    suspend operator fun invoke(route: Route): Result<Route> {

        return when (
            val validationResult = routeValidator(
                id = route.id,
                originWarehouse = route.originWarehouse,
                destinationWarehouse = route.destinationWarehouse,
                distanceKm = route.distanceKm,
                typicalDelayMin = route.typicalDelayMin
            )
        ) {
            ValidationResult.Success ->
                routeRepository.add(route)

            is ValidationResult.Failure ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}