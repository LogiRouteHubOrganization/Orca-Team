package domain.usecase.crud.warehouse

import domain.exception.EntityValidationException
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.validator.ValidationResult
import domain.validator.rule.IdValidator

class GetWarehouseByIdUseCase(
    private val warehouseRepository: WarehouseRepository
) {

    suspend operator fun invoke(
        id: String
    ): Result<Warehouse?> {

        return when (
            val validationResult = IdValidator(
                field = "warehouseId",
                allowedPrefixes = setOf("WH-")
            ).validate(id)
        ) {
            ValidationResult.Success ->
                warehouseRepository.findById(id)

            is ValidationResult.Failure ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}