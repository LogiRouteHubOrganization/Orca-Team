package domain.model.request

import domain.model.Route
import domain.model.Warehouse

data class PackageJourney(
    val packageId: String,
    val originWarehouse: Warehouse,
    val destinationWarehouse: Warehouse,
    val warehouses: List<Warehouse>,
    val routes: List<Route>,
    val totalDistanceKm: Double,
    val totalTypicalDelayMin: Int
)
