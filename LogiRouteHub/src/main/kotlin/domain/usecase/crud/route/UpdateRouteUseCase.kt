package domain.usecase.crud.route

import domain.exception.EntityValidationException
import domain.exception.ResourceNotFoundException
import domain.model.Route
import domain.repository.RouteRepository
import domain.validator.RouteValidator
import domain.validator.ValidationResult

class UpdateRouteUseCase(
    private val routeRepository: RouteRepository
) {

    suspend operator fun invoke(
        id: String,
        distanceKm: Double? = null,
        typicalDelayMin: Int? = null
    ): Result<Unit> {

        val validation = RouteValidator.validateUpdate(
            id = id,
            distanceKm = distanceKm,
            typicalDelayMin = typicalDelayMin
        )

        if (validation is ValidationResult.Invalid) {
            return Result.failure(
                EntityValidationException(
                    validation.violations.joinToString("; ") {
                        "${it.field}: ${it.message}"
                    }
                )
            )
        }

        val currentRoute = routeRepository.findById(id)
            ?: return Result.failure(
                ResourceNotFoundException(
                    "Route not found: $id"
                )
            )

        val updatedRoute = Route(
            id = currentRoute.id,
            originWarehouse = currentRoute.originWarehouse,
            destinationWarehouse = currentRoute.destinationWarehouse,
            distanceKm = distanceKm ?: currentRoute.distanceKm,
            typicalDelayMin = typicalDelayMin ?: currentRoute.typicalDelayMin
        )

        routeRepository.update(updatedRoute)

        return Result.success(Unit)
    }
}