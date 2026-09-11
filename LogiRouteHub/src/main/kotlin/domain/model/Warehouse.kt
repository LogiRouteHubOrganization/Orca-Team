package domain.model

class Warehouse(
    val id: String,
    val name: String,
    val regionalZone: String,
    val latitude: Double,
    val longitude: Double
) {
    private val cargoQueueList = mutableListOf<Package>()
    private val outgoingRoutesList = mutableListOf<Route>()
    private val stationedVehiclesList = mutableListOf<Vehicle>()

    val cargoQueue: List<Package>
        get() = cargoQueueList

    val outgoingRoutes: List<Route>
        get() = outgoingRoutesList

    val stationedVehicles: List<Vehicle>
        get() = stationedVehiclesList

    fun addPackage(logisticsPackage: Package) {
        cargoQueueList.add(logisticsPackage)
    }

    fun removePackage(logisticsPackage: Package) {
        cargoQueueList.remove(logisticsPackage)
    }

    fun addRoute(route: Route) {
        outgoingRoutesList.add(route)
    }

    fun addVehicle(vehicle: Vehicle) {
        stationedVehiclesList.add(vehicle)
    }

    fun removeVehicle(vehicle: Vehicle) {
        stationedVehiclesList.remove(vehicle)
    }

    fun updateVehicleAtIndex(
        vehicleIndex: Int,
        vehicle: Vehicle
    ) {
        stationedVehiclesList[vehicleIndex] = vehicle
    }

    fun updatePackageAtIndex(
        packageIndex: Int,
        logisticsPackage: Package
    ) {
        cargoQueueList[packageIndex] = logisticsPackage
    }

    fun restoreCargoQueue(previousQueue: List<Package>) {
        cargoQueueList.clear()
        cargoQueueList.addAll(previousQueue)
    }

    fun removeRoute(route: Route) {
        outgoingRoutesList.remove(route)
    }

    fun updateRouteAtIndex(
        routeIndex: Int,
        route: Route
    ) {
        outgoingRoutesList[routeIndex] = route
    }
}
