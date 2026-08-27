package domain.usecase

import domain.model.Package
import domain.model.Vehicle
import domain.model.Warehouse

class DispatchVehicleUseCase {

    operator fun invoke(
        vehicle: Vehicle,
        warehouse: Warehouse
    ): List<Package> {

        val remainingCapacity =
            vehicle.maxCapacityKg - vehicle.currentLoadKg()

        val packagesToDispatch = warehouse.cargoQueue
            .filter { logisticsPackage ->
                logisticsPackage.weightByKg <= remainingCapacity
            }
            .takeWhile { logisticsPackage ->
                logisticsPackage.weightByKg <= remainingCapacity
            }

        packagesToDispatch.forEach { logisticsPackage ->
            vehicle.addPackage(logisticsPackage)
            warehouse.removePackage(logisticsPackage)
        }

        return packagesToDispatch
    }
}