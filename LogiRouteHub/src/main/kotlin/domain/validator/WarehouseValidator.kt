package domain.validator

import domain.model.Warehouse
import java.util.UUID

object WarehouseValidator {

    fun validateCreate(warehouse: Warehouse): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        validateId(warehouse.id, violations)

        if (warehouse.name.isBlank()) {
            violations.add(
                FieldViolation("name", "Warehouse name must not be blank")
            )
        }

        if (warehouse.regionalZone.isBlank()) {
            violations.add(
                FieldViolation("regionalZone", "Regional zone must not be blank")
            )
        }

        validateLatitude(warehouse.latitude, violations)
        validateLongitude(warehouse.longitude, violations)

        return if (violations.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(violations)
        }
    }

    fun validateUpdate(
        id: String,
        name: String?,
        regionalZone: String?,
        latitude: Double?,
        longitude: Double?
    ): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        validateId(id, violations)

        if (
            name == null &&
            regionalZone == null &&
            latitude == null &&
            longitude == null
        ) {
            violations.add(
                FieldViolation(
                    "update",
                    "At least one property must be provided for update"
                )
            )
        }

        if (name != null && name.isBlank()) {
            violations.add(
                FieldViolation("name", "Warehouse name must not be blank")
            )
        }

        if (regionalZone != null && regionalZone.isBlank()) {
            violations.add(
                FieldViolation("regionalZone", "Regional zone must not be blank")
            )
        }

        if (latitude != null) {
            validateLatitude(latitude, violations)
        }

        if (longitude != null) {
            validateLongitude(longitude, violations)
        }

        return if (violations.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(violations)
        }
    }

    fun validateId(id: String, violations: MutableList<FieldViolation>) {
        if (id.isBlank()) {
            violations.add(
                FieldViolation("id", "Warehouse ID must not be blank")
            )
            return
        }

        val hasValidPrefix = id.startsWith("WH-")
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
                    "Warehouse ID must start with WH- or be a valid UUID"
                )
            )
        }
    }

    private fun validateLatitude(
        latitude: Double,
        violations: MutableList<FieldViolation>
    ) {
        if (latitude !in -90.0..90.0) {
            violations.add(
                FieldViolation(
                    "latitude",
                    "Latitude must be between -90 and 90"
                )
            )
        }
    }

    private fun validateLongitude(
        longitude: Double,
        violations: MutableList<FieldViolation>
    ) {
        if (longitude !in -180.0..180.0) {
            violations.add(
                FieldViolation(
                    "longitude",
                    "Longitude must be between -180 and 180"
                )
            )
        }
    }
}