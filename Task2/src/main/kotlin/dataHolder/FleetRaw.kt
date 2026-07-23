package dataHolder

data class FleetRaw(
    val vehicleIds: String,
    val currentHubId: String,
    val maxCapacityKg: Double,
    val costPerKm: Double
)