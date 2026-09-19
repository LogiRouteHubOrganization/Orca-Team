package domain.dispatcher
import domain.model.Route
import domain.model.Vehicle

class VehicleCoverageMapper {

    fun map(
        vehicles: List<Vehicle>,
        routes: List<Route>
    ): List<VehicleCoverage> {

        return vehicles.map { vehicle ->

            val coveredZones = routes
                .filter { route ->
                    route.originWarehouse.id == vehicle.currentWarehouse.id
                }
                .map { route ->
                    route.destinationWarehouse.regionalZone
                }
                .toSet()

            VehicleCoverage(
                vehicle = vehicle,
                coveredZones = coveredZones
            )
        }
    }
}