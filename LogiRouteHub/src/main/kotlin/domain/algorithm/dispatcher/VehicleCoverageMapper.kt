package domain.algorithm.dispatcher
import domain.model.Route
import domain.model.Vehicle

class VehicleCoverageMapper {

    fun prepareVehicleCoverage(
        vehicles: List<Vehicle>,
        routes: List<Route>
    ): List<VehicleCoverage> {
        return vehicles.map { vehicle ->
            val coveredZones = extractCoveredZonesForVehicle(vehicle, routes)
            VehicleCoverage(
                vehicle = vehicle,
                coveredZones = coveredZones
            )
        }
    }

    private fun extractCoveredZonesForVehicle(
        vehicle: Vehicle,
        routes: List<Route>
    ): Set<String> {
        return routes
            .filter { route -> route.originWarehouse.id == vehicle.currentWarehouse.id }
            .map { route -> route.destinationWarehouse.regionalZone }
            .toSet()
    }
}
