package domain.usecase.crud.route

import domain.exception.EntityValidationException
import domain.exception.ResourceNotFoundException
import domain.repository.RouteRepository
import domain.validator.FieldViolation
import domain.validator.RouteValidator

class DeleteRouteUseCase(
    private val routeRepository: RouteRepository
) {

    suspend operator fun invoke(
        routeId: String
    ): Result<Unit> {

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
            ?: return Result.failure(
                ResourceNotFoundException(
                    "Route not found: $routeId"
                )
            )

        routeRepository.delete(route.id)

        return Result.success(Unit)
    }
}