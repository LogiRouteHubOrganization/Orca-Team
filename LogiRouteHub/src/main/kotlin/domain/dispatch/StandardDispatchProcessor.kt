package domain.dispatch

import domain.exception.InvalidCargoException
import domain.exception.VehicleCapacityExceededException
import domain.model.Package
import domain.model.Vehicle

class StandardDispatchProcessor : BaseDispatchProcessor() {

    override fun validateCargo(
        logisticsPackage: Package,
        vehicle: Vehicle
    ) {
        if (logisticsPackage.weightByKg <= 0) {
            throw InvalidCargoException(
                "Package ${logisticsPackage.id} must have a positive weight."
            )
        }

        if (vehicle.maxCapacityKg < logisticsPackage.weightByKg) {
            throw VehicleCapacityExceededException(
                "Vehicle ${vehicle.id} does not have enough capacity " +
                        "for package ${logisticsPackage.id}."
            )
        }
    }

    override fun reserveVehicleCapacity(
        logisticsPackage: Package,
        vehicle: Vehicle
    ) {
        vehicle.addPackage(logisticsPackage)
    }

    override fun notifyDispatchStatus(
        logisticsPackage: Package,
        vehicle: Vehicle
    ) {
        println(
            "STANDARD DISPATCH: Package ${logisticsPackage.id} " +
                    "assigned and in transit via vehicle ${vehicle.id}."
        )
    }
}