package domain.validator

import domain.model.Route
import java.util.UUID

object RouteValidator {

    fun validateCreate(route: Route): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        validateId(route.id, violations)

        if (route.distanceKm <= 0) {
            violations.add(
                FieldViolation(
                    "distanceKm",
                    "Distance must be greater than 0"
                )
            )
        }

        if (route.typicalDelayMin < 0) {
            violations.add(
                FieldViolation(
                    "typicalDelayMin",
                    "Typical delay cannot be negative"
                )
            )
        }

        return buildResult(violations)
    }

    fun validateUpdate(
        id: String,
        distanceKm: Double?,
        typicalDelayMin: Int?
    ): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        validateId(id, violations)

        if (distanceKm == null && typicalDelayMin == null) {
            violations.add(
                FieldViolation(
                    "update",
                    "At least one property must be provided for update"
                )
            )
        }

        if (distanceKm != null && distanceKm <= 0) {
            violations.add(
                FieldViolation(
                    "distanceKm",
                    "Distance must be greater than 0"
                )
            )
        }

        if (typicalDelayMin != null && typicalDelayMin < 0) {
            violations.add(
                FieldViolation(
                    "typicalDelayMin",
                    "Typical delay cannot be negative"
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
                    "Route ID must not be blank"
                )
            )
            return
        }

        val hasValidPrefix = id.startsWith("RT-")

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
                    "Route ID must start with RT- or be a valid UUID"
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