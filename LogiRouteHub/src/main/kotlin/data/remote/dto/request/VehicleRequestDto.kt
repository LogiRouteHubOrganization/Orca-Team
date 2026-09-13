package data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VehicleRequestDto(
    @SerialName("max_capacity_kg")
    val maxCapacityKg: Double,

    @SerialName("cost_per_km")
    val costPerKm: Double,

    @SerialName("current_warehouse_id")
    val currentWarehouseId: String
)
