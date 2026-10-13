package domain.command

import domain.command.telemetry.CommandTelemetry
import domain.command.telemetry.DispatchState
import domain.extension.runCatchingCancellable
import domain.model.Package
import domain.model.Vehicle
import domain.model.Warehouse
import domain.model.updateinput.VehicleUpdateModel
import domain.model.updateinput.WarehouseUpdateModel
import domain.model.request.DispatchVehicleRequest
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository
import domain.usecase.DispatchVehicleUseCase

data class DispatchVehicleCommandDependencies(
    val dispatchVehicleUseCase: DispatchVehicleUseCase,
    val vehicleRepository: VehicleRepository,
    val warehouseRepository: WarehouseRepository,
    val telemetry: CommandTelemetry
)

class DispatchVehicleCommand(
    private val dependencies: DispatchVehicleCommandDependencies,
    private val request: DispatchVehicleRequest
) : Command {

    private var vehicle: Vehicle? = null
    private var warehouse: Warehouse? = null

    private var previousVehiclePackages: List<Package>? = null
    private var previousWarehouseQueue: List<Package>? = null

    private var afterVehiclePackages: List<Package>? = null
    private var afterWarehouseQueue: List<Package>? = null

    override val commandType = "DispatchVehicleCommand"

    override suspend fun execute(): Result<Unit> {
        return runCatchingCancellable  {
            val currentVehicle = dependencies.vehicleRepository
                .findById(request.vehicleId)
                .getOrThrow() ?: error("Vehicle not found: ${request.vehicleId}")

            val currentWarehouse = dependencies.warehouseRepository
                .findById(request.warehouseId)
                .getOrThrow() ?: error("Warehouse not found: ${request.warehouseId}")

            vehicle = currentVehicle
            warehouse = currentWarehouse

            previousVehiclePackages = currentVehicle.assignedPackages.toList()
            previousWarehouseQueue = currentWarehouse.cargoQueue.toList()

            val dispatchedPackages = dependencies.dispatchVehicleUseCase(request).getOrThrow()
            if (dispatchedPackages.isEmpty()) {
                error("No packages were dispatched")
            }

            afterVehiclePackages = currentVehicle.assignedPackages.toList()
            afterWarehouseQueue = currentWarehouse.cargoQueue.toList()

            recordStateTransition()
            Unit
        }
    }

    override suspend fun undo(): Result<Unit> {
        return runCatchingCancellable  {
            val (executedVehicle, executedWarehouse) = getExecutedEntities()
            val oldVehiclePackages = previousVehiclePackages ?: error("Previous vehicle state is unavailable")
            val oldWarehouseQueue = previousWarehouseQueue ?: error("Previous warehouse state is unavailable")
            val currentVehiclePackages = afterVehiclePackages ?: error("After vehicle state is unavailable")
            val currentWarehouseQueue = afterWarehouseQueue ?: error("After warehouse state is unavailable")

            applyDispatchStateAndSave(executedVehicle, oldVehiclePackages, executedWarehouse, oldWarehouseQueue)
            recordTelemetryState(currentVehiclePackages, currentWarehouseQueue, oldVehiclePackages, oldWarehouseQueue)
            Unit
        }
    }

    override suspend fun redo(): Result<Unit> {
        return runCatchingCancellable  {
            val (executedVehicle, executedWarehouse) = getExecutedEntities()
            val oldVehiclePackages = previousVehiclePackages ?: error("Previous vehicle state is unavailable")
            val oldWarehouseQueue = previousWarehouseQueue ?: error("Previous warehouse state is unavailable")
            val newVehiclePackages = afterVehiclePackages ?: error("After vehicle state is unavailable")
            val newWarehouseQueue = afterWarehouseQueue ?: error("After warehouse state is unavailable")

            applyDispatchStateAndSave(executedVehicle, newVehiclePackages, executedWarehouse, newWarehouseQueue)
            recordTelemetryState(oldVehiclePackages, oldWarehouseQueue, newVehiclePackages, newWarehouseQueue)
            Unit
        }
    }

    private fun getExecutedEntities(): Pair<Vehicle, Warehouse> {
        val executedVehicle = vehicle ?: error("Command has not been executed")
        val executedWarehouse = warehouse ?: error("Command has not been executed")
        return Pair(executedVehicle, executedWarehouse)
    }

    private suspend fun applyDispatchStateAndSave(
        vehicle: Vehicle,
        vehiclePackages: List<Package>,
        warehouse: Warehouse,
        warehouseQueue: List<Package>
    ) {
        vehicle.restoreAssignedPackages(vehiclePackages)
        warehouse.restoreCargoQueue(warehouseQueue)

        dependencies.vehicleRepository.update(
            vehicle.id,
            VehicleUpdateModel(
                maxCapacityKg = vehicle.maxCapacityKg,
                costPerKm = vehicle.costPerKm,
                currentWarehouseId = vehicle.currentWarehouse?.id
            )
        ).getOrThrow()

        dependencies.warehouseRepository.update(
            warehouse.id,
            WarehouseUpdateModel(
                name = warehouse.name,
                regionalZone = warehouse.regionalZone,
                latitude = warehouse.latitude,
                longitude = warehouse.longitude
            )
        ).getOrThrow()
    }

    private fun recordTelemetryState(
        beforeVehicle: List<Package>,
        beforeWarehouse: List<Package>,
        afterVehicle: List<Package>,
        afterWarehouse: List<Package>
    ) {
        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState = DispatchState(
                vehiclePackageIds = beforeVehicle.map { it.id },
                warehouseQueuePackageIds = beforeWarehouse.map { it.id }
            ),
            afterState = DispatchState(
                vehiclePackageIds = afterVehicle.map { it.id },
                warehouseQueuePackageIds = afterWarehouse.map { it.id }
            )
        )
    }

    private fun recordStateTransition() {
        recordTelemetryState(
            beforeVehicle = previousVehiclePackages.orEmpty(),
            beforeWarehouse = previousWarehouseQueue.orEmpty(),
            afterVehicle = afterVehiclePackages.orEmpty(),
            afterWarehouse = afterWarehouseQueue.orEmpty()
        )
    }
}


