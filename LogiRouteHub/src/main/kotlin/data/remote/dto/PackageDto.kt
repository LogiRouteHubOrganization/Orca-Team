package data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PackageDto(
    val id: String,

    @SerialName("weight_by_kg")
    val weightByKg: Double,

    val priority: String,

    @SerialName("origin_warehouse_id")
    val originWarehouseId: String,

    @SerialName("destination_warehouse_id")
    val destinationWarehouseId: String
)