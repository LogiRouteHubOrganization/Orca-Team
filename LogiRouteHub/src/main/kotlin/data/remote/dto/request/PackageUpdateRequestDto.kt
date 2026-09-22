package data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PackageUpdateRequestDto(


    @SerialName("weight_by_kg")
    val weightByKg: Double? = null,

    val priority: String? = null,

    @SerialName("origin_warehouse_id")
    val originWarehouseId: String? = null,

    @SerialName("destination_warehouse_id")
    val destinationWarehouseId: String? = null
)
