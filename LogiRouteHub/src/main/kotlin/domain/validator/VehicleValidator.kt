package domain.validator

import domain.model.Vehicle
import java.util.UUID

object VehicleValidator {

    fun validateCreate(vehicle: Vehicle): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        validateId(vehicle.id, violations)

        if (vehicle.maxCapacityKg <= 0) {
            violations.add(
                FieldViolation(
                    "maxCapacityKg",
                    "Maximum capacity must be greater than 0"
                )
            )
        }

        if (vehicle.costPerKm <= 0) {
            violations.add(
                FieldViolation(
                    "costPerKm",
                    "Cost per kilometer must be greater than 0"
                )
            )
        }

        return buildResult(violations)
    }

    fun validateUpdate(
        id: String,
        maxCapacityKg: Double?,
        costPerKm: Double?
    ): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        validateId(id, violations)

        if (maxCapacityKg == null && costPerKm == null) {
            violations.add(
                FieldViolation(
                    "update",
                    "At least one property must be provided for update"
                )
            )
        }

        if (maxCapacityKg != null && maxCapacityKg <= 0) {
            violations.add(
                FieldViolation(
                    "maxCapacityKg",
                    "Maximum capacity must be greater than 0"
                )
            )
        }

        if (costPerKm != null && costPerKm <= 0) {
            violations.add(
                FieldViolation(
                    "costPerKm",
                    "Cost per kilometer must be greater than 0"
                )
            )
        }

        return buildResult(violations)
    }

    fun validateId(
        id: String,
        violations: MutableList<FieldViolation>
    ) {
        if (id.isBlank()) {
            violations.add(
                FieldViolation(
                    "id",
                    "Vehicle ID must not be blank"
                )
            )
            return
        }

        val hasValidPrefix = id.startsWith("TRK-")

        val isUuid = try {
            UUID.fromString(id)
            true
        } catch (_: IllegalArgumentException) {
            false
        }

        if (!hasValidPrefix && !isUuid) {
            violations.add(
                FieldViolation(
                    "id",
                    "Vehicle ID must start with TRK- or be a valid UUID"
                )
            )
        }
    }

    private fun buildResult(
        violations: List<FieldViolation>
    ): ValidationResult {
        return if (violations.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(violations)
        }
    }
}