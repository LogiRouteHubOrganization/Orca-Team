package domain.model

data class Vehicle(
    val id: String,
    val maxCapacityKg: Double,
    val costPerKm: Double,
    val currentWarehouse: Warehouse

) {
    private val cargoList = mutableListOf<Package>()

    val cargo: List<Package>
        get() = cargoList

    fun addPackage(logisticsPackage: Package) {
        cargoList.add(logisticsPackage)
    }

    fun currentLoadKg(): Double {
        return cargoList.sumOf { logisticsPackage ->
            logisticsPackage.weightByKg
        }
    }
}
