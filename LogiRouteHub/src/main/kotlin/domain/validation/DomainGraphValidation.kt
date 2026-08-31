package domain.validation

import domain.model.Warehouse

fun validateDomainGraph(warehousesById: Map<String, Warehouse>) {
    println("\n=== Domain Graph Validation ===")

    if (warehousesById.isEmpty()) {
        println("Validation skipped: No warehouses were built.")
        return
    }
    warehousesById.values.forEach { warehouse ->
        println("\nWarehouse: ${warehouse.id}")

        warehouse.cargoQueue.forEach { pkg ->
            println("Package ${pkg.id} linked correctly: ${pkg.originWarehouse === warehouse}")
        }
        warehouse.stationedVehicles.forEach { vehicle ->
            println("Vehicle ${vehicle.id} linked correctly: ${vehicle.currentWarehouse === warehouse}")
        }
        warehouse.outgoingRoutes.forEach { route ->
            println("Route ${route.id} linked correctly: ${route.originWarehouse === warehouse}")
        }
    }
    println("\nDomain graph validation completed.")
}
