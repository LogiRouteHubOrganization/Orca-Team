package domain.usecase.crud.route

import domain.exception.EntityValidationException
import domain.exception.ResourceNotFoundException
import domain.model.Route
import domain.repository.RouteRepository
import domain.validator.FieldViolation
import domain.validator.RouteValidator

class GetRouteByIdUseCase(
    private val routeRepository: RouteRepository
) {

    suspend operator fun invoke(
        routeId: String
    ): Result<Route> {

        val violations = mutableListOf<FieldViolation>()

        RouteValidator.validateId(
            id = routeId,
            violations = violations
        )

        if (violations.isNotEmpty()) {
            return Result.failure(
                EntityValidationException(
                    violations.joinToString("; ") {
                        "${it.field}: ${it.message}"
                    }
                )
            )
        }

        val route = routeRepository.findById(routeId)

        return if (route != null) {
            Result.success(route)
        } else {
            Result.failure(
                ResourceNotFoundException(
                    "Route not found: $routeId"
                )
            )
        }
    }
}