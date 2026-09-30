package domain.model
import domain.exception.EntityValidationException
import domain.validator.ResultValidator
import domain.validator.vehicle.CreateVehicleValidator

data class Vehicle(
    val id: String,
    val maxCapacityKg: Double,
    val costPerKm: Double,
    var currentWarehouse: Warehouse
) {
    init {
        val validationResult = CreateVehicleValidator().invoke(
            id = id,
            maxCapacityKg = maxCapacityKg,
            costPerKm = costPerKm,
            currentWarehouse = currentWarehouse
        )

        if (validationResult is ResultValidator.Invalid) {
            throw EntityValidationException(validationResult.errors)
        }
    }

    private val packages = mutableListOf<Package>()

    val assignedPackages: List<Package>
        get() = packages

    fun addPackage(logisticsPackage: Package) {
        packages.add(logisticsPackage)
    }

    fun calculateTotalLoadedWeightKg(): Double {
        return packages.sumOf { it.weightByKg }
    }

    fun restoreAssignedPackages(previousPackages: List<Package>) {
        packages.clear()
        packages.addAll(previousPackages)
    }

}

