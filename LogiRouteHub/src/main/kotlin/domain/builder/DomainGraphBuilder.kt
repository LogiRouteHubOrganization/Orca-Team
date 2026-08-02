package domain.builder

import dataHolder.FleetRaw
import dataHolder.LogisticsPackageRaw
import dataHolder.RouteRaw
import dataHolder.WarehouseRaw
import domain.model.Package
import domain.model.Route
import domain.model.Vehicle
import domain.model.Warehouse

class DomainGraphBuilder {

    fun buildGraph(
        input: GraphBuilderInput
    ): Map<String, Warehouse> {

        val warehouses = createWarehouses(input.warehouses)

        val warehouseIndex = createWarehouseIndex(warehouses)

        buildPackages(input.packages, warehouseIndex)

        buildVehicles(input.vehicles, warehouseIndex)

        buildRoutes(input.routes, warehouseIndex)

        return warehouseIndex
    }

    private fun createWarehouses(rawWarehouses: List<WarehouseRaw>): List<Warehouse> {

        val warehouses = mutableListOf<Warehouse>()
        for (rawWarehouse in rawWarehouses) {
            val warehouse = Warehouse(
                id = rawWarehouse.warehouseId,
                name = rawWarehouse.warehouseName,
                regionalZone = rawWarehouse.regionalZone,
                latitude = rawWarehouse.latitude,
                longitude = rawWarehouse.longitude
            )
            warehouses.add(warehouse)
        }
        return warehouses
    }

    private fun createWarehouseIndex(warehouses: List<Warehouse>): Map<String, Warehouse> {

        return warehouses.associateBy { warehouse ->
            warehouse.id
        }
    }

    private fun getWarehouseById(
        warehouseId: String,
        warehousesById: Map<String, Warehouse>
    ): Warehouse? {

        val warehouse = warehousesById[warehouseId]

        if (warehouse == null) {
            println("Warning: Warehouse not found -> $warehouseId")
        }
        return warehouse
    }

    private fun buildPackages(
        rawPackages: List<LogisticsPackageRaw>,
        warehousesById: Map<String, Warehouse>
    ) {
        for (rawPackage in rawPackages) {

            val originWarehouse = getWarehouseById(
                rawPackage.originHubId,
                warehousesById
            )
            val destinationWarehouse = getWarehouseById(
                rawPackage.destinationHubId,
                warehousesById
            )
            if (originWarehouse != null && destinationWarehouse != null) {
                val logisticsPackage = createPackageFromRawData(
                    rawPackage,
                    originWarehouse,
                    destinationWarehouse
                )
                originWarehouse.addPackage(logisticsPackage)
            }
        }
    }

    private fun createPackageFromRawData(
        rawPackage: LogisticsPackageRaw,
        originWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): Package {
        return Package(
            id = rawPackage.packageId,
            weightByKg = rawPackage.packageWeightByKg,
            priority = rawPackage.packagePriority,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }

    private fun buildVehicles(
        rawVehicles: List<FleetRaw>,
        warehousesById: Map<String, Warehouse>
    ) {
        for (rawVehicle in rawVehicles) {
            val currentWarehouse = getWarehouseById(
                rawVehicle.currentHubId,
                warehousesById
            )
            if (currentWarehouse != null) {
                val vehicle = createVehicleFromRawData(
                    rawVehicle,
                    currentWarehouse
                )
                currentWarehouse.addVehicle(vehicle)
            }
        }
    }

    private fun createVehicleFromRawData(
        rawVehicle: FleetRaw,
        currentWarehouse: Warehouse
    ): Vehicle {
        return Vehicle(
            id = rawVehicle.vehicleId,
            maxCapacityKg = rawVehicle.maxCapacityKg,
            costPerKm = rawVehicle.costPerKm,
            currentWarehouse = currentWarehouse
        )
    }

    private fun buildRoutes(rawRoutes: List<RouteRaw>, warehousesById: Map<String, Warehouse>) {
        for (rawRoute in rawRoutes) {
            val originWarehouse = getWarehouseById(
                rawRoute.originHubId,
                warehousesById
            )
            val destinationWarehouse = getWarehouseById(
                rawRoute.destinationHubId,
                warehousesById
            )
            if (originWarehouse != null && destinationWarehouse != null) {
                val route =
                    createRouteFromRawData(
                        rawRoute,
                        originWarehouse,
                        destinationWarehouse
                    )
                originWarehouse.addRoute(route)
            }
        }
    }

    private fun createRouteFromRawData(
        rawRoute: RouteRaw,
        originWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): Route {
        return Route(
            id = rawRoute.routeId,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse,
            distanceKm = rawRoute.distanceKm,
            typicalDelayMin = rawRoute.typicalDelayMin
        )
    }
}