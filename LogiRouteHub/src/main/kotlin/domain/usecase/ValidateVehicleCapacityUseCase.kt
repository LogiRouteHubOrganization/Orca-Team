package domain.usecase

import domain.model.Package
import domain.model.Vehicle

class ValidateVehicleCapacityUseCase {

    operator fun invoke(
        vehicle: Vehicle,
        packages: List<Package>
    ): Boolean {
        val totalPackageWeight = packages.sumOf { it.weightByKg }
        val currentLoadedWeight = vehicle.calculateTotalLoadedWeightKg()

        return (currentLoadedWeight + totalPackageWeight) <= vehicle.maxCapacityKg
    }
}
