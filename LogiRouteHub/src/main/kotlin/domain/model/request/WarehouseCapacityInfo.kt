package domain.model.request

import domain.model.Warehouse

data class WarehouseCapacityInfo(
    val warehouse: Warehouse,
    val totalCargoWeightKg: Double,
    val totalVehicleCapacityKg: Double
)
