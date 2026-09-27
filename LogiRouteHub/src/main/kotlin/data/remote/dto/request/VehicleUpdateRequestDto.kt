package data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VehicleUpdateRequestDto(

    @SerialName("max_capacity_kg")
    val maxCapacityKg: Double? = null,

    @SerialName("cost_per_km")
    val costPerKm: Double? = null,

    @SerialName("current_warehouse_id")
    val currentWarehouseId: String? = null
)
