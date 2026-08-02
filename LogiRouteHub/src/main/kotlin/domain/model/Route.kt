package domain.model

data class Route(
    val id: String,
    val originWarehouse: Warehouse,
    val destinationWarehouse: Warehouse,
    val distanceKm: Double,
    val typicalDelayMin: Int
)