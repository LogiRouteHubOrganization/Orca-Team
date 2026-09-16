package domain.command

import domain.command.telemetry.CommandTelemetry
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

    override suspend fun execute(): Boolean {
        val currentVehicle =
            dependencies.vehicleRepository.findById(request.vehicleId)
                ?: return false

        val currentWarehouse =
            dependencies.warehouseRepository.findById(request.warehouseId)
                ?: return false

        vehicle = currentVehicle
        warehouse = currentWarehouse

        previousVehiclePackages =
            currentVehicle.assignedPackages.toList()

        previousWarehouseQueue =
            currentWarehouse.cargoQueue.toList()

        val dispatchedPackages =
            dependencies.dispatchVehicleUseCase(request)

        if (dispatchedPackages.isEmpty()) {
            return false
        }

        afterVehiclePackages =
            currentVehicle.assignedPackages.toList()

        afterWarehouseQueue =
            currentWarehouse.cargoQueue.toList()

        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState = createStateDescription(
                previousVehiclePackages,
                previousWarehouseQueue
            ),
            afterState = createStateDescription(
                afterVehiclePackages,
                afterWarehouseQueue
            )
        )

        return true
    }

    override suspend fun undo() {
        val executedVehicle = vehicle ?: return
        val executedWarehouse = warehouse ?: return

        val oldVehiclePackages =
            previousVehiclePackages ?: return

        val oldWarehouseQueue =
            previousWarehouseQueue ?: return

        val currentVehiclePackages =
            afterVehiclePackages ?: return

        val currentWarehouseQueue =
            afterWarehouseQueue ?: return

        executedVehicle.restoreAssignedPackages(oldVehiclePackages)

        executedWarehouse.restoreCargoQueue(oldWarehouseQueue)

        dependencies.vehicleRepository.update(
            executedVehicle.id,
            VehicleUpdateModel(
                maxCapacityKg = executedVehicle.maxCapacityKg,
                costPerKm = executedVehicle.costPerKm,
                currentWarehouseId = executedVehicle.currentWarehouse?.id
            )
        )

        dependencies.warehouseRepository.update(
            executedWarehouse.id,
            WarehouseUpdateModel(
                name = executedWarehouse.name,
                regionalZone = executedWarehouse.regionalZone,
                latitude = executedWarehouse.latitude,
                longitude = executedWarehouse.longitude
            )
        )

        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState = createStateDescription(
                currentVehiclePackages,
                currentWarehouseQueue
            ),
            afterState = createStateDescription(
                oldVehiclePackages,
                oldWarehouseQueue
            )
        )
    }

    override suspend fun redo() {
        val executedVehicle = vehicle ?: return
        val executedWarehouse = warehouse ?: return

        val oldVehiclePackages =
            previousVehiclePackages ?: return

        val oldWarehouseQueue =
            previousWarehouseQueue ?: return

        val newVehiclePackages =
            afterVehiclePackages ?: return

        val newWarehouseQueue =
            afterWarehouseQueue ?: return

        executedVehicle.restoreAssignedPackages(newVehiclePackages)

        executedWarehouse.restoreCargoQueue(newWarehouseQueue)

        dependencies.vehicleRepository.update(
            executedVehicle.id,
            VehicleUpdateModel(
                maxCapacityKg = executedVehicle.maxCapacityKg,
                costPerKm = executedVehicle.costPerKm,
                currentWarehouseId = executedVehicle.currentWarehouse?.id
            )
        )

        dependencies.warehouseRepository.update(
            executedWarehouse.id,
            WarehouseUpdateModel(
                name = executedWarehouse.name,
                regionalZone = executedWarehouse.regionalZone,
                latitude = executedWarehouse.latitude,
                longitude = executedWarehouse.longitude
            )
        )

        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState = createStateDescription(
                oldVehiclePackages,
                oldWarehouseQueue
            ),
            afterState = createStateDescription(
                newVehiclePackages,
                newWarehouseQueue
            )
        )
    }

    private fun createStateDescription(
        vehiclePackages: List<Package>?,
        warehouseQueue: List<Package>?
    ): String {
        return "Vehicle packages count: ${vehiclePackages?.size}\n" +
                "Vehicle packages: ${vehiclePackages?.take(5)?.map { it.id }}\n" +
                "Warehouse queue count: ${warehouseQueue?.size}\n" +
                "Warehouse queue: ${warehouseQueue?.take(5)?.map { it.id }}"
    }
}
