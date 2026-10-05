package domain.usecase

import domain.dispatch.ExpressDispatchProcessor
import domain.dispatch.StandardDispatchProcessor
import domain.model.Package
import domain.model.request.DispatchVehicleRequest
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository

class DispatchVehicleUseCase(
    private val vehicleRepository: VehicleRepository,
    private val warehouseRepository: WarehouseRepository
) {

    suspend operator fun invoke(
        input: DispatchVehicleRequest
    ): Result<List<Package>> = runCatching {

        val vehicle = vehicleRepository
            .findById(input.vehicleId)
            .getOrThrow()
            ?: return@runCatching emptyList()

        val warehouse = warehouseRepository
            .findById(input.warehouseId)
            .getOrThrow()
            ?: return@runCatching emptyList()

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

            val dispatchProcessor = when (logisticsPackage.priority) {
                "URGENT" -> ExpressDispatchProcessor()
                "STANDARD", "LOW" -> StandardDispatchProcessor()

                else -> throw IllegalArgumentException(
                    "Unsupported package priority: ${logisticsPackage.priority}"
                )
            }

            dispatchProcessor.processDispatch(
                logisticsPackage,
                vehicle
            )

            warehouse.removePackage(logisticsPackage)
        }

        packagesToDispatch
    }
}