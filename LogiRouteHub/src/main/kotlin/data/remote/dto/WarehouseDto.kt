package data.remote.dto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WarehouseDto(
    val id: String,
    val name: String,

    @SerialName("regional_zone")
    val regionalZone: String,

    val latitude: Double,
    val longitude: Double
)
