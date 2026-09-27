package data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WarehouseUpdateRequestDto(

    val name: String? = null,

    @SerialName("regional_zone")
    val regionalZone: String? = null,

    val latitude: Double? = null,

    val longitude: Double? = null
)
