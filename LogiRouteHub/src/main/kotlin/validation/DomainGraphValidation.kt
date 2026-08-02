package validation

import domain.model.Warehouse

fun validateDomainGraph(warehousesById: Map<String, Warehouse>) {
    println("\n=== Domain Graph Validation ===")

    if (warehousesById.isEmpty()) {
        println("Validation skipped: No warehouses were built.")
        return
    }

    warehousesById.values.forEach { warehouse ->
        println("\nWarehouse: ${warehouse.warehouseId}")

        warehouse.cargoQueue.forEach { logisticsPackage ->
            println("Package ${logisticsPackage.packageId} linked correctly: ${logisticsPackage.originWarehouse === warehouse}")
        }
        warehouse.stationedVehicles.forEach { vehicle ->
            println("Vehicle ${vehicle.vehicleId} linked correctly: ${vehicle.currentWarehouse === warehouse}")
        }
        warehouse.outgoingRoutes.forEach { route ->
            println("Route ${route.routeId} linked correctly: ${route.originWarehouse === warehouse}")
        }
    }

    println("\nDomain graph validation completed.")
}