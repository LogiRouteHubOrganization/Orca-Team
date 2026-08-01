package builder

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
        rawWarehouses: List<WarehouseRaw>,
        rawPackages: List<LogisticsPackageRaw>,
        rawVehicles: List<FleetRaw>,
        rawRoutes: List<RouteRaw>
    ): Map<String, Warehouse> {

        val warehouses = createWarehouses(rawWarehouses)

        val warehousesById =
            createWarehouseIndex(warehouses)

        createPackages(
            rawPackages,
            warehousesById
        )

        createVehicles(
            rawVehicles,
            warehousesById
        )

        createRoutes(
            rawRoutes,
            warehousesById
        )

        return warehousesById
    }

    private fun createWarehouses(
        rawWarehouses: List<WarehouseRaw>
    ): List<Warehouse> {

        val warehouses = mutableListOf<Warehouse>()

        for (rawWarehouse in rawWarehouses) {

            val warehouse = Warehouse(
                warehouseId = rawWarehouse.warehouseId,
                warehouseName = rawWarehouse.warehouseName,
                regionalZone = rawWarehouse.regionalZone
            )

            warehouses.add(warehouse)
        }

        return warehouses
    }

    private fun createWarehouseIndex(
        warehouses: List<Warehouse>
    ): Map<String, Warehouse> {

        return warehouses.associateBy { warehouse ->
            warehouse.warehouseId
        }
    }

    private fun getWarehouseById(
        warehouseId: String,
        warehousesById: Map<String, Warehouse>
    ): Warehouse? {

        val warehouse =
            warehousesById[warehouseId]

        if (warehouse == null) {
            println(
                "Warning: Warehouse not found -> $warehouseId"
            )
        }

        return warehouse
    }

    private fun createPackages(
        rawPackages: List<LogisticsPackageRaw>,
        warehousesById: Map<String, Warehouse>
    ): List<Package> {

        val packages = mutableListOf<Package>()

        for (rawPackage in rawPackages) {

            val originWarehouse =
                getWarehouseById(
                    rawPackage.originHubId,
                    warehousesById
                )

            val destinationWarehouse =
                getWarehouseById(
                    rawPackage.destinationHubId,
                    warehousesById
                )

            if (
                originWarehouse != null &&
                destinationWarehouse != null
            ) {

                val logisticsPackage =
                    createPackage(
                        rawPackage,
                        originWarehouse,
                        destinationWarehouse
                    )

                originWarehouse.addPackage(
                    logisticsPackage
                )

                packages.add(logisticsPackage)
            }
        }

        return packages
    }

    private fun createPackage(
        rawPackage: LogisticsPackageRaw,
        originWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): Package {

        return Package(
            packageId = rawPackage.packageId,
            packageWeightByKg =
                rawPackage.packageWeightByKg,
            packagePriority =
                rawPackage.packagePriority,
            originWarehouse = originWarehouse,
            destinationWarehouse =
                destinationWarehouse
        )
    }

    private fun createVehicles(
        rawVehicles: List<FleetRaw>,
        warehousesById: Map<String, Warehouse>
    ): List<Vehicle> {

        val vehicles = mutableListOf<Vehicle>()

        for (rawVehicle in rawVehicles) {

            val currentWarehouse =
                getWarehouseById(
                    rawVehicle.currentHubId,
                    warehousesById
                )

            if (currentWarehouse != null) {

                val vehicle =
                    createVehicle(
                        rawVehicle,
                        currentWarehouse
                    )

                currentWarehouse.addVehicle(vehicle)

                vehicles.add(vehicle)
            }
        }

        return vehicles
    }

    private fun createVehicle(
        rawVehicle: FleetRaw,
        currentWarehouse: Warehouse
    ): Vehicle {

        return Vehicle(
            vehicleId = rawVehicle.vehicleId,
            maxCapacityKg =
                rawVehicle.maxCapacityKg,
            costPerKm = rawVehicle.costPerKm,
            currentWarehouse = currentWarehouse
        )
    }

    private fun createRoutes(
        rawRoutes: List<RouteRaw>,
        warehousesById: Map<String, Warehouse>
    ): List<Route> {

        val routes = mutableListOf<Route>()

        for (rawRoute in rawRoutes) {

            val originWarehouse =
                getWarehouseById(
                    rawRoute.originHubId,
                    warehousesById
                )

            val destinationWarehouse =
                getWarehouseById(
                    rawRoute.destinationHubId,
                    warehousesById
                )

            if (
                originWarehouse != null &&
                destinationWarehouse != null
            ) {

                val route =
                    createRoute(
                        rawRoute,
                        originWarehouse,
                        destinationWarehouse
                    )

                originWarehouse.addRoute(route)

                routes.add(route)
            }
        }

        return routes
    }

    private fun createRoute(
        rawRoute: RouteRaw,
        originWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): Route {

        return Route(
            routeId = rawRoute.routeId,
            originWarehouse = originWarehouse,
            destinationWarehouse =
                destinationWarehouse,
            distanceKm = rawRoute.distanceKm,
            typicalDelayMin =
                rawRoute.typicalDelayMin
        )
    }
}