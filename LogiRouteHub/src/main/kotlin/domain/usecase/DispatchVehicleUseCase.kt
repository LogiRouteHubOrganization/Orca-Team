package domain.usecase

import domain.dispatch.ExpressDispatchProcessor
import domain.dispatch.StandardDispatchProcessor
import domain.model.Package
import domain.model.request.DispatchVehicleRequest
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository
import domain.extension.runCatchingCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class DispatchVehicleUseCase(
    private val vehicleRepository: VehicleRepository,
    private val warehouseRepository: WarehouseRepository,
    private val optimizeCargoByWeightUseCase: OptimizeCargoByWeightUseCase
) {

    suspend operator fun invoke(
        input: DispatchVehicleRequest
    ): Result<List<Package>> = runCatchingCancellable {

        val (vehicle, warehouse) = coroutineScope {
            val vehicleDeferred = async { vehicleRepository.findById(input.vehicleId).getOrThrow() }
            val warehouseDeferred = async { warehouseRepository.findById(input.warehouseId).getOrThrow() }

            Pair(vehicleDeferred.await(), warehouseDeferred.await())
        }

        if (vehicle == null || warehouse == null) {
            return@runCatchingCancellable emptyList()
        }

        val supportedPriorities = setOf("URGENT", "STANDARD", "LOW")

        warehouse.cargoQueue.forEach { logisticsPackage ->
            require(logisticsPackage.priority in supportedPriorities) {
                "Unsupported package priority: ${logisticsPackage.priority}"
            }
        }

        val availableCapacity = vehicle.maxCapacityKg - vehicle.calculateTotalLoadedWeightKg()

        val packagesToDispatch = optimizeCargoByWeightUseCase.optimize(
            packages = warehouse.cargoQueue,
            capacityKg = availableCapacity
        )

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