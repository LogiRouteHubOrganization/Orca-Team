package domain.model.request

import domain.model.Warehouse

data class PotentialDeliveryBottleneck(
    val warehouse: Warehouse,
    val totalCargoWeightKg: Double,
    val totalVehicleCapacityKg: Double,
    val capacityGapKg: Double
)

