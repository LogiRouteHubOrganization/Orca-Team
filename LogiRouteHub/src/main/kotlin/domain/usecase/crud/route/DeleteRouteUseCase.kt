package domain.usecase.crud.route

import domain.exception.EntityValidationException
import domain.repository.RouteRepository
import domain.validator.ValidationResult
import domain.validator.rule.IdValidator

class DeleteRouteUseCase(
    private val routeRepository: RouteRepository
) {
    suspend operator fun invoke(routeId: String): Result<Unit> {

        return when (
            val validationResult = IdValidator(
                field = "routeId",
                allowedPrefixes = setOf("RT-")
            ).validate(routeId)
        ) {
            ValidationResult.Valid ->
                routeRepository.delete(routeId)

            is ValidationResult.Invalid ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}
