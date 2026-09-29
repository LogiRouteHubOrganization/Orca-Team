package domain.model

import domain.exception.EntityValidationException
import domain.validator.ResultVaildator
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

        if (validationResult is ResultVaildator.Invalid) {
            throw EntityValidationException(validationResult.errors)
        }
    }
}
