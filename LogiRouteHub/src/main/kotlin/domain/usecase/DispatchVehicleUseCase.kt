package domain.usecase

import domain.model.Package
import domain.model.Vehicle
import domain.model.Warehouse

class DispatchVehicleUseCase {

    operator fun invoke(
        vehicle: Vehicle,
        warehouse: Warehouse
    ): List<Package> {

        val availableCapacity =
            vehicle.maxCapacityKg -
                    vehicle.calculateTotalLoadedWeightKg()

        val packagesToDispatch = warehouse.cargoQueue.fold(
            emptyList<Package>() to availableCapacity
        ) { (selectedPackages, remainingCapacity), logisticsPackage ->

            if (logisticsPackage.weightByKg <= remainingCapacity) {

                (selectedPackages + logisticsPackage) to
                        (remainingCapacity - logisticsPackage.weightByKg)

            } else {
                selectedPackages to remainingCapacity
            }
        }.first
        packagesToDispatch.forEach { logisticsPackage ->
            vehicle.addPackage(logisticsPackage)
            warehouse.removePackage(logisticsPackage)
        }
        return packagesToDispatch
    }
}
