package domain.model

data class Vehicle(
    val id: String,
    val maxCapacityKg: Double,
    val costPerKm: Double,
    var currentWarehouse: Warehouse
) {
    private val packages = mutableListOf<Package>()

    val assignedPackages: List<Package>
        get() = packages

    fun addPackage(logisticsPackage: Package) {
        packages.add(logisticsPackage)
    }

    fun calculateTotalLoadedWeightKg(): Double {
        return packages.sumOf { it.weightByKg }
    }
}
