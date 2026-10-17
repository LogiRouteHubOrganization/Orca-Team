package domain.usecase.crud.warehouse

import domain.exception.EntityValidationException
import domain.repository.WarehouseRepository
import domain.validator.ResultValidator
import domain.validator.rule.IdValidator

class DeleteWarehouseUseCase(
    private val warehouseRepository: WarehouseRepository
) {

    suspend operator fun invoke(
        id: String
    ): Result<Unit> {

        return when (
            val validationResult = IdValidator(
                field = "warehouseId",
                allowedPrefixes = setOf("WH-")
            ).validate(id)
        ) {
            ResultValidator.Valid ->
                warehouseRepository.delete(id)

            is ResultValidator.Invalid ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}

