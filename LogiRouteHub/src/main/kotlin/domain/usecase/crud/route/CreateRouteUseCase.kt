package domain.usecase.crud.route

import domain.exception.EntityValidationException
import domain.model.Route
import domain.repository.RouteRepository
import domain.validator.RouteValidator
import domain.validator.ValidationResult

class CreateRouteUseCase(
    private val routeRepository: RouteRepository
) {

    suspend operator fun invoke(route: Route): Result<Unit> {

        return when (val validation = RouteValidator.validateCreate(route)) {
            ValidationResult.Valid -> {
                routeRepository.add(route)
                Result.success(Unit)
            }

            is ValidationResult.Invalid -> {
                Result.failure(
                    EntityValidationException(
                        validation.violations.joinToString("; ") {
                            "${it.field}: ${it.message}"
                        }
                    )
                )
            }
        }
    }
}