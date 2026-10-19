package domain.model.updateinput

data class RouteUpdateModel(
    val originWarehouseId: String? = null,
    val destinationWarehouseId: String? = null,
    val distanceKm: Double? = null,
    val typicalDelayMin: Int? = null
)

