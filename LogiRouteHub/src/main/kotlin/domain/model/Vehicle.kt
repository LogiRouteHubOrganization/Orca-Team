package domain.model
import domain.exception.EntityValidationException
import domain.exception.VehicleCapacityExceededException
import domain.validator.ResultValidator
import domain.validator.vehicle.CreateVehicleValidator

data class Vehicle(
    val id: String,
    val maxCapacityKg: Double,
    val costPerKm: Double,
    var currentWarehouse: Warehouse,
    val maxVolumeM3: Double? = null
) {
    init {
        val validationResult = CreateVehicleValidator().invoke(
            id = id,
            maxCapacityKg = maxCapacityKg,
            costPerKm = costPerKm,
            currentWarehouse = currentWarehouse,
            maxVolumeM3 = maxVolumeM3
        )
        if (validationResult is ResultValidator.Invalid) {
            throw EntityValidationException(validationResult.errors)
        }
    }

    private val packages = mutableListOf<Package>()

    val assignedPackages: List<Package>
        get() = packages

    fun addPackage(logisticsPackage: Package) {
        val currentLoad = calculateTotalLoadedWeightKg()
        val newLoad = currentLoad + logisticsPackage.weightByKg

        if (newLoad > maxCapacityKg) {
            throw VehicleCapacityExceededException(
                "Vehicle $id does not have enough capacity " +
                        "for package ${logisticsPackage.id}."
            )
        }

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


