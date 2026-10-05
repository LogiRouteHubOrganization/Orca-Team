package domain.usecase

import domain.algorithm.knapsack.KnapsackCargoOptimizer
import domain.dispatch.ExpressDispatchProcessor
import domain.dispatch.StandardDispatchProcessor
import domain.model.Package
import domain.model.request.DispatchVehicleRequest
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository

class DispatchVehicleUseCase(
    private val vehicleRepository: VehicleRepository,
    private val warehouseRepository: WarehouseRepository,
    private val knapsackCargoOptimizer: KnapsackCargoOptimizer
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

        // Select the optimal cargo using the Knapsack algorithm
        val packagesToDispatch = knapsackCargoOptimizer.optimize(
            packages = warehouse.cargoQueue,
            capacityKg = availableCapacity
        )

        // Dispatch selected packages through the dispatch pipeline
        packagesToDispatch.forEach { logisticsPackage ->

            val dispatchProcessor = when (logisticsPackage.priority) {
                "URGENT" -> ExpressDispatchProcessor()

                "STANDARD", "LOW" -> StandardDispatchProcessor()

                else -> throw IllegalArgumentException(
                    "Unsupported package priority: ${logisticsPackage.priority}"
                )
            }

            println("Before dispatch: ${logisticsPackage.state::class.simpleName}")

            dispatchProcessor.processDispatch(
                logisticsPackage,
                vehicle
            )

            println("After dispatch: ${logisticsPackage.state::class.simpleName}")

            warehouse.removePackage(logisticsPackage)
        }

        packagesToDispatch
    }
}