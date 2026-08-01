package domain.model

data class Route(
    val routeId: String,
    val originWarehouse: Warehouse,
    val destinationWarehouse: Warehouse,
    val distanceKm: Double,
    val typicalDelayMin: Int
)
{
    override fun toString(): String {
        return "Route(" +
                "routeId='$routeId', " +
                "originWarehouseId='${originWarehouse.warehouseId}', " +
                "destinationWarehouseId='${destinationWarehouse.warehouseId}', " +
                "distanceKm=$distanceKm, " +
                "typicalDelayMin=$typicalDelayMin" +
                ")"
    }
}