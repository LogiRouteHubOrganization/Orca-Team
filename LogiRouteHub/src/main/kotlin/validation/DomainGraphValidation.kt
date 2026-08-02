package validation

import domain.model.Warehouse

fun validateDomainGraph(
    warehousesById: Map<String, Warehouse>
) {
    println()
    println("=== Domain Graph Validation ===")

    if (warehousesById.isEmpty()) {
        println("Validation skipped: No warehouses were built.")
        return
    }

    for (warehouse in warehousesById.values) {

        println()
        println("Warehouse: ${warehouse.id}")

        validatePackageReferences(warehouse)
        validateVehicleReferences(warehouse)
        validateRouteReferences(warehouse)
    }
    println()
    println("Domain graph validation completed.")
}

private fun validatePackageReferences(
    warehouse: Warehouse
) {
    for (logisticsPackage in warehouse.cargoQueue) {

        val hasCorrectOriginWarehouseReference = logisticsPackage.originWarehouse === warehouse

        println("Package ${logisticsPackage.id} linked correctly: " + hasCorrectOriginWarehouseReference)
    }
}

private fun validateVehicleReferences(
    warehouse: Warehouse
) {
    for (vehicle in warehouse.stationedVehicles) {

        val hasCorrectCurrentWarehouseReference = vehicle.currentWarehouse === warehouse

        println("Vehicle ${vehicle.id} linked correctly: " + hasCorrectCurrentWarehouseReference)
    }
}

private fun validateRouteReferences(
    warehouse: Warehouse
) {
    for (route in warehouse.outgoingRoutes) {

        val hasCorrectOriginWarehouseReference = route.originWarehouse === warehouse

        println(
            "Route ${route.id} linked correctly: " + hasCorrectOriginWarehouseReference
        )
    }
}