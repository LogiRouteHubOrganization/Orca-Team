package domain.model.updateinput

data class VehicleUpdateModel(
    val maxCapacityKg: Double? = null,
    val costPerKm: Double? = null,
    val currentWarehouseId: String? = null
)
