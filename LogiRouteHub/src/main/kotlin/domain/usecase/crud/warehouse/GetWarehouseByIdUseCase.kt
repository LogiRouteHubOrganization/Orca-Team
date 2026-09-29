package domain.usecase.crud.warehouse

import domain.exception.EntityValidationException
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.validator.ResultVaildator
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
            ResultVaildator.Valid ->
                warehouseRepository.findById(id)

            is ResultVaildator.Invalid ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}
