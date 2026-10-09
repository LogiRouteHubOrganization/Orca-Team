package domain.usecase

import domain.dispatch.ExpressDispatchProcessor
import domain.dispatch.StandardDispatchProcessor
import domain.model.Package
import domain.model.request.DispatchVehicleRequest
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository

class DispatchVehicleUseCase(
    private val vehicleRepository: VehicleRepository,
    private val warehouseRepository: WarehouseRepository,
    private val optimizeCargoByWeightUseCase: OptimizeCargoByWeightUseCase
) {

    suspend operator fun invoke(
        input: DispatchVehicleRequest
    ): Result<List<Package>> = runCatching {

        val vehicle = vehicleRepository.findById(input.vehicleId).getOrThrow() ?: return@runCatching emptyList()

        val warehouse = warehouseRepository.findById(input.warehouseId).getOrThrow() ?: return@runCatching emptyList()

        val supportedPriorities = setOf("URGENT", "STANDARD", "LOW")

        warehouse.cargoQueue.forEach { logisticsPackage ->
            require(logisticsPackage.priority in supportedPriorities) {
                "Unsupported package priority: ${logisticsPackage.priority}"
            }
        }

        val availableCapacity = vehicle.maxCapacityKg - vehicle.calculateTotalLoadedWeightKg()

        // Select the optimal cargo using the Knapsack algorithm
        val packagesToDispatch = optimizeCargoByWeightUseCase.optimize(
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

            dispatchProcessor.processDispatch(logisticsPackage, vehicle)
            println("After dispatch: ${logisticsPackage.state::class.simpleName}")

            warehouse.removePackage(logisticsPackage)
        }
        packagesToDispatch
    }
}
