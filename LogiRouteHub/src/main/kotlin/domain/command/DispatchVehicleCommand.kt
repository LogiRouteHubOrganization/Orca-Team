package domain.command

import domain.model.Package
import domain.model.Vehicle
import domain.model.Warehouse
import domain.model.request.DispatchVehicleRequest
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository
import domain.usecase.DispatchVehicleUseCase
import domain.command.telemetry.CommandTelemetry

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

    override fun execute(): Boolean {
        val currentVehicle =
            dependencies.vehicleRepository.findById(request.vehicleId) ?: return false

        val currentWarehouse =
            dependencies.warehouseRepository.findById(request.warehouseId) ?: return false

        vehicle = currentVehicle
        warehouse = currentWarehouse

        previousVehiclePackages = currentVehicle.assignedPackages.toList()
        previousWarehouseQueue = currentWarehouse.cargoQueue.toList()

        val dispatchedPackages =
            dependencies.dispatchVehicleUseCase(request)

        if (dispatchedPackages.isEmpty()) return false

        afterVehiclePackages = currentVehicle.assignedPackages.toList()
        afterWarehouseQueue = currentWarehouse.cargoQueue.toList()

        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState = "Vehicle packages: $previousVehiclePackages\n" +
                          "Warehouse queue: $previousWarehouseQueue",
            afterState = "Vehicle packages: $afterVehiclePackages\n" +
                         "Warehouse queue: $afterWarehouseQueue"
        )
        return true
    }

    override fun undo() {
        val executedVehicle = vehicle ?: return
        val executedWarehouse = warehouse ?: return

        val oldVehiclePackages = previousVehiclePackages ?: return
        val oldWarehouseQueue = previousWarehouseQueue ?: return

        val newVehiclePackages = afterVehiclePackages ?: return
        val newWarehouseQueue = afterWarehouseQueue ?: return

        executedVehicle.restoreAssignedPackages(oldVehiclePackages)
        executedWarehouse.restoreCargoQueue(oldWarehouseQueue)

        dependencies.vehicleRepository.update(executedVehicle)
        dependencies.warehouseRepository.update(executedWarehouse)

        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState = "Vehicle packages: $newVehiclePackages\n" +
                         "Warehouse queue: $newWarehouseQueue",
            afterState = "Vehicle packages: $oldVehiclePackages\n" +
                         "Warehouse queue: $oldWarehouseQueue"
        )
    }

    override fun redo() {
        val executedVehicle = vehicle ?: return
        val executedWarehouse = warehouse ?: return

        val newVehiclePackages = afterVehiclePackages ?: return
        val newWarehouseQueue = afterWarehouseQueue ?: return

        val oldVehiclePackages = previousVehiclePackages ?: return
        val oldWarehouseQueue = previousWarehouseQueue ?: return

        executedVehicle.restoreAssignedPackages(newVehiclePackages)
        executedWarehouse.restoreCargoQueue(newWarehouseQueue)

        dependencies.vehicleRepository.update(executedVehicle)
        dependencies.warehouseRepository.update(executedWarehouse)

        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState = "Vehicle packages: $oldVehiclePackages\n" +
                         "Warehouse queue: $oldWarehouseQueue",
            afterState = "Vehicle packages: $newVehiclePackages\n" +
                         "Warehouse queue: $newWarehouseQueue"
        )
    }
}
