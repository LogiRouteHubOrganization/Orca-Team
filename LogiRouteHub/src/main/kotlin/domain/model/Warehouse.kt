package domain.model

import domain.exception.EntityValidationException
import domain.validator.ValidationResult
import domain.validator.rule.IdValidator
import domain.validator.rule.LatitudeValidator
import domain.validator.rule.LongitudeValidator
import domain.validator.rule.NonBlankStringValidator

class Warehouse(
    val id: String,
    name: String,
    regionalZone: String,
    latitude: Double,
    longitude: Double
) {

    init {
        val validationResults = listOf(
            IdValidator(
                field = "id",
                allowedPrefixes = setOf("WH-")
            ).validate(id),

            NonBlankStringValidator(
                field = "name"
            ).validate(name),

            NonBlankStringValidator(
                field = "regionalZone"
            ).validate(regionalZone),

            LatitudeValidator(
                field = "latitude"
            ).validate(latitude),

            LongitudeValidator(
                field = "longitude"
            ).validate(longitude)
        )

        val errors = validationResults
            .filterIsInstance<ValidationResult.Failure>()
            .flatMap { it.errors }

        if (errors.isNotEmpty()) {
            throw EntityValidationException(errors)
        }
    }

    var name: String = name
        private set

    var regionalZone: String = regionalZone
        private set

    var latitude: Double = latitude
        private set

    var longitude: Double = longitude
        private set

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

    fun updateDetails(
        name: String,
        regionalZone: String,
        latitude: Double,
        longitude: Double
    ) {
        this.name = name
        this.regionalZone = regionalZone
        this.latitude = latitude
        this.longitude = longitude
    }
}