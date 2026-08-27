package domain.model

class Warehouse(
    val id: String, val name: String, val regionalZone: String, val latitude: Double, val longitude: Double
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

    fun addRoute(route: Route) {
        outgoingRoutesList.add(route)
    }

    fun addVehicle(vehicle: Vehicle) {
        stationedVehiclesList.add(vehicle)
    }

    fun updatePackageAtIndex(
        packageIndex: Int, logisticsPackage: Package
    ) {
        cargoQueueList[packageIndex] = logisticsPackage
    }

    fun removePackage(logisticsPackage: Package) {
        cargoQueueList.remove(logisticsPackage)
    }
}
