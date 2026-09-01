package domain.model.request

import domain.model.Warehouse

data class ReroutePackageRequest(
    val warehouse: Warehouse,
    val packageId: String,
    val newDestinationWarehouse: Warehouse
)
