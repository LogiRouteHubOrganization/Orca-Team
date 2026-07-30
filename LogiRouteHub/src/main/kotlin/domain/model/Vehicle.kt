package domain.model

data class Vehicle(
    val vehicleId: String,
    val maxCapacityKg: Double,
    val costPerKm: Double,
    val currentWarehouse: Warehouse
)