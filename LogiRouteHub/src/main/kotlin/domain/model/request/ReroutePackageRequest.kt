package domain.model.request

import domain.model.Warehouse

data class ReroutePackageRequest(
    val warehouseId: String,
    val packageId: String,
    val newDestinationWarehouse: Warehouse
)
