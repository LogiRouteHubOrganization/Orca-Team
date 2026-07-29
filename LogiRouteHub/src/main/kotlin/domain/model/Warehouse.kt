package domain.model

class Warehouse(val id: String,
                val name: String,
                val regionalZone: String)

{
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


}