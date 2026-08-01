package builder
import java.io.File
import dataHolder.LogisticsPackageRaw
import dataHolder.warehouseRaw
import domain.model.Package
import domain.model.Warehouse
import dataHolder.FleetRaw
import domain.model.Vehicle
import dataHolder.RoutesRaw
import domain.model.Route
import parser.FleetParser
import parser.parseRoutes
import parser.parseWarehousesFile
import parser.PackageParser


class DomainGraphBuilder {

    fun buildGraph(
        rawWarehouses: List<warehouseRaw>,
        rawPackages: List<LogisticsPackageRaw>,
        rawVehicles: List<FleetRaw>,
        rawRoutes: List<RoutesRaw>
    ): Map<String, Warehouse> {

        val warehouses = createWarehouses(rawWarehouses)
        val warehouseIndex = createWarehouseIndex(warehouses)

        createPackages(rawPackages, warehouseIndex)
        createVehicles(rawVehicles, warehouseIndex)
        createRoutes(rawRoutes, warehouseIndex)

        return warehouseIndex
    }

    fun createWarehouses(rawWarehouses: List<warehouseRaw>): List<Warehouse> {

        val warehouses = mutableListOf<Warehouse>()
        for (rawWarehouse in rawWarehouses) {
            val warehouse = Warehouse(
                warehouseId = rawWarehouse.warehouseId,
                warehouseName = rawWarehouse.warehousename,
                regionalZone = rawWarehouse.regionalZone
            )
            warehouses.add(warehouse)
        }
        return warehouses
    }
    fun createWarehouseIndex(warehouses: List<Warehouse>): Map<String, Warehouse> {
        return warehouses.associateBy { warehouse ->
            warehouse.warehouseId
        }
    }

    fun getRequiredWarehouse(warehouseId: String, warehousesById: Map<String, Warehouse>): Warehouse? {
        val warehouse = warehousesById[warehouseId]
        if (warehouse == null) {
            println("Warehouse not found: $warehouseId")
        }
        return warehouse
    }
    fun createPackageFromRawData(
        rawPackage: LogisticsPackageRaw,
        originWarehouse: Warehouse,
        destinationWarehouse: Warehouse): Package {
        return Package(
            packageId = rawPackage.packageId,
            packageWeightByKg = rawPackage.packageWeightByKg,
            packagePriority = rawPackage.packagePriority,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }

    fun linkPackageToOriginWarehouse(
        logisticsPackage: Package,
        originWarehouse: Warehouse
    ) {
        originWarehouse.addPackage(logisticsPackage)
    }

    fun createPackages(
        rawPackages: List<LogisticsPackageRaw>,
        warehousesById: Map<String, Warehouse>
    ): List<Package> {

        val packages = mutableListOf<Package>()

        for (rawPackage in rawPackages) {

            val originWarehouse = getRequiredWarehouse(
                rawPackage.originHubId,
                warehousesById
            )
            val destinationWarehouse = getRequiredWarehouse(
                rawPackage.destinationHubId,
                warehousesById
            )

            if (
                originWarehouse != null &&
                destinationWarehouse != null
            ) {

                val logisticsPackage = createPackageFromRawData(
                    rawPackage,
                    originWarehouse,
                    destinationWarehouse
                )

                linkPackageToOriginWarehouse(logisticsPackage, originWarehouse)

                packages.add(logisticsPackage)
            }
        }

        return packages
    }
    fun createVehicleFromRawData(
        rawVehicle: FleetRaw,
        currentWarehouse: Warehouse
    ): Vehicle {

        return Vehicle(
            vehicleId = rawVehicle.vehicleIds,
            maxCapacityKg = rawVehicle.maxCapacityKg,
            costPerKm = rawVehicle.costPerKm,
            currentWarehouse = currentWarehouse
        )
    }
    fun linkVehicleToCurrentWarehouse(
        vehicle: Vehicle,
        currentWarehouse: Warehouse
    ) {
        currentWarehouse.addVehicle(vehicle)
    }
    fun createVehicles(
        rawVehicles: List<FleetRaw>,
        warehousesById: Map<String, Warehouse>
    ): List<Vehicle> {

        val vehicles = mutableListOf<Vehicle>()

        for (rawVehicle in rawVehicles) {

            val currentWarehouse = getRequiredWarehouse(
                rawVehicle.currentHubId,
                warehousesById
            )
            if (currentWarehouse != null) {
                val vehicle = createVehicleFromRawData(
                    rawVehicle,
                    currentWarehouse
                )
                linkVehicleToCurrentWarehouse(
                    vehicle,
                    currentWarehouse
                )
                vehicles.add(vehicle)
            }
        }
        return vehicles
    }
    fun createRouteFromRawData(
        rawRoute: RoutesRaw,
        originWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): Route {

        val distanceKm = rawRoute.distanceKm.toDouble()
        val typicalDelayMin = rawRoute.typicalDelayMin.toInt()

        return Route(
            routeId = rawRoute.routeId,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse,
            distanceKm = distanceKm,
            typicalDelayMin = typicalDelayMin
        )
    }
    fun linkRouteToOriginWarehouse(
        route: Route,
        originWarehouse: Warehouse
    ) {
        originWarehouse.addRoute(route)
    }
    fun createRoutes(
        rawRoutes: List<RoutesRaw>,
        warehousesById: Map<String, Warehouse>
    ): List<Route> {

        val routes = mutableListOf<Route>()

        for (rawRoute in rawRoutes) {

            val originWarehouse = getRequiredWarehouse(
                rawRoute.originHubId,
                warehousesById
            )

            val destinationWarehouse = getRequiredWarehouse(
                rawRoute.destinationHubId,
                warehousesById
            )
            if (
                originWarehouse != null &&
                destinationWarehouse != null
            ) {

                val route = createRouteFromRawData(
                    rawRoute,
                    originWarehouse,
                    destinationWarehouse
                )

                linkRouteToOriginWarehouse(
                    route,
                    originWarehouse
                )

                routes.add(route)
            }
        }
        return routes
    }


}


val packageParser = PackageParser()

val packages = packageParser.readLogisticsPackageRowsFromFile(
    "src/main/resources/packages.csv"
)
fun buildGraphData() : Map<String, Warehouse> {

    val packageParser = PackageParser()
    val fleetParser = FleetParser()



            return DomainGraphBuilder().buildGraph(
                parseWarehousesFile(File("src/main/resources/warehouses.csv")),
                packageParser.readLogisticsPackageRowsFromFile("src/main/resources/packages.csv"),
                fleetParser.readFleetRowsFromFile("src/main/resources/fleet.csv"),
                parseRoutes("src/main/resources/routes.csv")
            )
}
fun checkMemoryReference() {
    val graph = buildGraphData()
    val warehouse = graph.values.first()

    check(warehouse === graph[warehouse.warehouseId])
    println("Memory Reference Test: PASSED")
}

fun checkWarehousePrint() {
    val warehouse = buildGraphData().values.first()

    println(warehouse)
    println("Print Test: PASSED")
}

fun checkGraphBuild() {
    val graph = buildGraphData()

    check(graph.isNotEmpty())
    println("Graph Build Test: PASSED")
}

fun checkGraphPerformance() {
    val start = System.currentTimeMillis()

    val graph = buildGraphData()

    println("Build Time: ${System.currentTimeMillis() - start} ms")

    check(graph.isNotEmpty())
    println("Performance Test: PASSED")
}

fun main() {
    checkMemoryReference()
    checkWarehousePrint()
    checkGraphBuild()
    checkGraphPerformance()
}