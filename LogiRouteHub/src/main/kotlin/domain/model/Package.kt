package domain.model
data class Package(
    val packageId: String,
    val packageWeightByKg: Double,
    val packagePriority: String,
    val originWarehouse: Warehouse,
    val destinationWarehouse: Warehouse
)
{
    override fun toString(): String {
        return "Package(" +
                "packageId='$packageId', " +
                "packageWeightByKg=$packageWeightByKg, " +
                "packagePriority='$packagePriority', " +
                "originWarehouseId='${originWarehouse.warehouseId}', " +
                "destinationWarehouseId='${destinationWarehouse.warehouseId}'" +
                ")"
    }
}