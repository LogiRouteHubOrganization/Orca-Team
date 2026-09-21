package domain.command

import domain.command.telemetry.CommandTelemetry
import domain.command.telemetry.DispatchState
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
        return runCatching {
            val currentVehicle =
                dependencies.vehicleRepository
                    .findById(request.vehicleId)
                    .getOrThrow()
                    ?: error("Vehicle not found: ${request.vehicleId}")

            val currentWarehouse =
                dependencies.warehouseRepository
                    .findById(request.warehouseId)
                    .getOrThrow()
                    ?: error("Warehouse not found: ${request.warehouseId}")

            vehicle = currentVehicle
            warehouse = currentWarehouse

            previousVehiclePackages =
                currentVehicle.assignedPackages.toList()

            previousWarehouseQueue =
                currentWarehouse.cargoQueue.toList()
            val dispatchedPackages =
                dependencies.dispatchVehicleUseCase(request)
                    .getOrThrow()

            if (dispatchedPackages.isEmpty()) {
                error("No packages were dispatched")
            }

            afterVehiclePackages =
                currentVehicle.assignedPackages.toList()

            afterWarehouseQueue =
                currentWarehouse.cargoQueue.toList()

            recordStateTransition()

            Unit
        }
    }

    override suspend fun undo(): Result<Unit> {
        return runCatching {
            val executedVehicle =
                vehicle ?: error("Command has not been executed")

            val executedWarehouse =
                warehouse ?: error("Command has not been executed")

            val oldVehiclePackages =
                previousVehiclePackages
                    ?: error("Previous vehicle state is unavailable")

            val oldWarehouseQueue =
                previousWarehouseQueue
                    ?: error("Previous warehouse state is unavailable")

            val currentVehiclePackages =
                afterVehiclePackages
                    ?: error("After vehicle state is unavailable")

            val currentWarehouseQueue =
                afterWarehouseQueue
                    ?: error("After warehouse state is unavailable")

            executedVehicle.restoreAssignedPackages(oldVehiclePackages)
            executedWarehouse.restoreCargoQueue(oldWarehouseQueue)

            dependencies.vehicleRepository.update(
                executedVehicle.id,
                VehicleUpdateModel(
                    maxCapacityKg = executedVehicle.maxCapacityKg,
                    costPerKm = executedVehicle.costPerKm,
                    currentWarehouseId = executedVehicle.currentWarehouse?.id
                )
            ).getOrThrow()

            dependencies.warehouseRepository.update(
                executedWarehouse.id,
                WarehouseUpdateModel(
                    name = executedWarehouse.name,
                    regionalZone = executedWarehouse.regionalZone,
                    latitude = executedWarehouse.latitude,
                    longitude = executedWarehouse.longitude
                )
            ).getOrThrow()

            dependencies.telemetry.recordStateTransition(
                commandType = commandType,
                beforeState = DispatchState(
                    vehiclePackageIds =
                        currentVehiclePackages.map { it.id },
                    warehouseQueuePackageIds =
                        currentWarehouseQueue.map { it.id }
                ),
                afterState = DispatchState(
                    vehiclePackageIds =
                        oldVehiclePackages.map { it.id },
                    warehouseQueuePackageIds =
                        oldWarehouseQueue.map { it.id }
                )
            )

            Unit
        }
    }

    override suspend fun redo(): Result<Unit> {
        return runCatching {
            val executedVehicle =
                vehicle ?: error("Command has not been executed")

            val executedWarehouse =
                warehouse ?: error("Command has not been executed")

            val oldVehiclePackages =
                previousVehiclePackages
                    ?: error("Previous vehicle state is unavailable")

            val oldWarehouseQueue =
                previousWarehouseQueue
                    ?: error("Previous warehouse state is unavailable")

            val newVehiclePackages =
                afterVehiclePackages
                    ?: error("After vehicle state is unavailable")

            val newWarehouseQueue =
                afterWarehouseQueue
                    ?: error("After warehouse state is unavailable")

            executedVehicle.restoreAssignedPackages(newVehiclePackages)
            executedWarehouse.restoreCargoQueue(newWarehouseQueue)

            dependencies.vehicleRepository.update(
                executedVehicle.id,
                VehicleUpdateModel(
                    maxCapacityKg = executedVehicle.maxCapacityKg,
                    costPerKm = executedVehicle.costPerKm,
                    currentWarehouseId = executedVehicle.currentWarehouse?.id
                )
            ).getOrThrow()

            dependencies.warehouseRepository.update(
                executedWarehouse.id,
                WarehouseUpdateModel(
                    name = executedWarehouse.name,
                    regionalZone = executedWarehouse.regionalZone,
                    latitude = executedWarehouse.latitude,
                    longitude = executedWarehouse.longitude
                )
            ).getOrThrow()

            dependencies.telemetry.recordStateTransition(
                commandType = commandType,
                beforeState = DispatchState(
                    vehiclePackageIds =
                        oldVehiclePackages.map { it.id },
                    warehouseQueuePackageIds =
                        oldWarehouseQueue.map { it.id }
                ),
                afterState = DispatchState(
                    vehiclePackageIds =
                        newVehiclePackages.map { it.id },
                    warehouseQueuePackageIds =
                        newWarehouseQueue.map { it.id }
                )
            )

            Unit
        }
    }

    private fun recordStateTransition() {
        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState = DispatchState(
                vehiclePackageIds =
                    previousVehiclePackages.orEmpty().map { it.id },
                warehouseQueuePackageIds =
                    previousWarehouseQueue.orEmpty().map { it.id }
            ),
            afterState = DispatchState(
                vehiclePackageIds =
                    afterVehiclePackages.orEmpty().map { it.id },
                warehouseQueuePackageIds =
                    afterWarehouseQueue.orEmpty().map { it.id }
            )
        )
    }
}