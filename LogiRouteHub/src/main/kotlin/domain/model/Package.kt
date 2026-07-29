package domain.model
data class Package(
    val packageId: String,
    val packageWeightByKg: Double,
    val packagePriority: String,
    val originWarehouse: Warehouse,
    val destinationWarehouse: Warehouse
)