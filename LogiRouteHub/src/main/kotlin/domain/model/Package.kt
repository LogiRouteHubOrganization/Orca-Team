package domain.model
data class Package(
    val id: String,
    val weightByKg: Double,
    val priority: String,
    val originWarehouse: Warehouse,
    val destinationWarehouse: Warehouse
)