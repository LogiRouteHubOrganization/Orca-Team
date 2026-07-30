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
                "RouteId='$routeId', " +
                "DistanceKm=$distanceKm, " +
                "OriginWarehouseId='${originWarehouse.warehouseId}', " +
                "DestinationWarehouseId='${destinationWarehouse.warehouseId}', " +
                ")"
    }
}