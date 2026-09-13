package data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RouteRequestDto(
    @SerialName("origin_warehouse_id")
    val originWarehouseId: String,

    @SerialName("destination_warehouse_id")
    val destinationWarehouseId: String,

    @SerialName("distance_km")
    val distanceKm: Double,

    @SerialName("typical_delay_min")
    val typicalDelayMin: Int
)