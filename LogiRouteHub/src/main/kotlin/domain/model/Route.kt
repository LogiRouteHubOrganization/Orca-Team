package domain.model

import domain.exception.EntityValidationException
import domain.validator.ValidationResult
import domain.validator.route.CreateRouteValidator

data class Route(
    val id: String,
    val originWarehouse: Warehouse,
    val destinationWarehouse: Warehouse,
    val distanceKm: Double,
    val typicalDelayMin: Int
) {

    init {
        val validationResult = CreateRouteValidator().invoke(
            id = id,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse,
            distanceKm = distanceKm,
            typicalDelayMin = typicalDelayMin
        )

        if (validationResult is ValidationResult.Failure) {
            throw EntityValidationException(validationResult.errors)
        }
    }
}
