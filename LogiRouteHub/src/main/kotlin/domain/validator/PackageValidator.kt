package domain.validator

import domain.model.Package
import java.util.UUID

object PackageValidator {

    private val allowedPriorities = setOf(
        "LOW",
        "STANDARD",
        "HIGH",
        "URGENT"
    )

    fun validateCreate(logisticsPackage: Package): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        validateId(logisticsPackage.id, violations)

        if (logisticsPackage.weightByKg <= 0) {
            violations.add(
                FieldViolation(
                    "weightByKg",
                    "Package weight must be greater than 0"
                )
            )
        }

        if (logisticsPackage.priority !in allowedPriorities) {
            violations.add(
                FieldViolation(
                    "priority",
                    "Priority must be one of: $allowedPriorities"
                )
            )
        }

        return buildResult(violations)
    }

    fun validateUpdate(
        id: String,
        weightByKg: Double?,
        priority: String?
    ): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        validateId(id, violations)

        if (weightByKg == null && priority == null) {
            violations.add(
                FieldViolation(
                    "update",
                    "At least one property must be provided for update"
                )
            )
        }

        if (weightByKg != null && weightByKg <= 0) {
            violations.add(
                FieldViolation(
                    "weightByKg",
                    "Package weight must be greater than 0"
                )
            )
        }

        if (priority != null && priority !in allowedPriorities) {
            violations.add(
                FieldViolation(
                    "priority",
                    "Priority must be one of: $allowedPriorities"
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
                    "Package ID must not be blank"
                )
            )
            return
        }

        val hasValidPrefix = id.startsWith("PKG-")

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
                    "Package ID must start with PKG- or be a valid UUID"
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