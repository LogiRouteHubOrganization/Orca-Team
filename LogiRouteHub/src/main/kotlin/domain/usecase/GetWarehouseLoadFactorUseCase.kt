package domain.usecasePackage
import domain.model.Warehouse
class GetWarehouseLoadFactorUseCase {
    var initialValueForTheSum =0.0
    val vehicleCapacity=0.0
    operator fun invoke(warehouse: Warehouse): Double {
        val totalPackageWeight = warehouse.cargoQueue
            .map { packageItem -> packageItem.weightByKg}
            .fold(initialValueForTheSum) { totalWeight, packageWeight ->
                totalWeight + packageWeight
            }
        val totalVehicleCapacity = warehouse.stationedVehicles
            .map { vehicle -> vehicle.maxCapacityKg }
            .fold(initialValueForTheSum) { totalCapacity, vehicleCapacity ->
                totalCapacity + vehicleCapacity
            }

        return if (totalVehicleCapacity == vehicleCapacity) {
            vehicleCapacity
        } else {
            totalPackageWeight / totalVehicleCapacity
        }
    }
}
