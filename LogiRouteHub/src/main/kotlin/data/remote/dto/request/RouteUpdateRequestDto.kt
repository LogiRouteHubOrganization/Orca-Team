package data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RouteUpdateRequestDto(

    @SerialName("origin_warehouse_id")
    val originWarehouseId: String? = null,

    @SerialName("destination_warehouse_id")
    val destinationWarehouseId: String? = null,

    @SerialName("distance_km")
    val distanceKm: Double? = null,

    @SerialName("typical_delay_min")
    val typicalDelayMin: Int? = null
)
