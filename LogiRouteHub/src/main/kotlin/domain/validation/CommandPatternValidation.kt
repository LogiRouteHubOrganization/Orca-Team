package domain.validation

import domain.command.AssignPackageToQueueCommand
import domain.command.AssignPackageToQueueCommandDependencies
import domain.command.CommandInvoker
import domain.command.DispatchVehicleCommand
import domain.command.DispatchVehicleCommandDependencies
import domain.command.ReroutePackageCommand
import domain.command.ReroutePackageCommandDependencies
import domain.model.Package
import domain.model.Vehicle
import domain.model.Warehouse
import domain.model.request.DispatchVehicleRequest
import domain.model.request.ReroutePackageRequest
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository
import domain.usecase.AssignPackageToCargoQueueUseCase
import domain.usecase.DispatchVehicleUseCase
import domain.usecase.ReroutePackageUseCase
import domain.usecase.SortingCargoQueueUseCase

private class ValidationWarehouseRepository(
    private val warehouseIndex: Map<String, Warehouse>
) : WarehouseRepository {

    override fun findById(warehouseId: String): Warehouse? {
        return warehouseIndex[warehouseId]
    }

    override fun findAll(): List<Warehouse> {
        return warehouseIndex.values.toList()
    }

    override fun update(warehouse: Warehouse) {
    }

    override fun addPackage(
        warehouse: Warehouse,
        logisticsPackage: Package
    ) {
        warehouse.addPackage(logisticsPackage)
    }
}

private class ValidationVehicleRepository(
    private val warehouseIndex: Map<String, Warehouse>
) : VehicleRepository {

    override fun findById(vehicleId: String): Vehicle? {
        return warehouseIndex.values
            .flatMap { warehouse -> warehouse.stationedVehicles }
            .firstOrNull { vehicle -> vehicle.id == vehicleId }
    }

    override fun update(vehicle: Vehicle) {
    }
}

fun validateCommandPattern(
    warehouseIndex: Map<String, Warehouse>
) {
    println()
    println("=== Command Pattern Validation ===")

    if (warehouseIndex.isEmpty()) {
        println("Validation skipped: No warehouses were built.")
        return
    }

    val warehouseRepository =
        ValidationWarehouseRepository(warehouseIndex)

    val vehicleRepository =
        ValidationVehicleRepository(warehouseIndex)

    val assignPackageToCargoQueueUseCase =
        AssignPackageToCargoQueueUseCase(
            sortCargoQueue = SortingCargoQueueUseCase(),
            warehouseRepository = warehouseRepository
        )

    val dispatchVehicleUseCase =
        DispatchVehicleUseCase(
            vehicleRepository = vehicleRepository,
            warehouseRepository = warehouseRepository
        )

    val reroutePackageUseCase =
        ReroutePackageUseCase(warehouseRepository = warehouseRepository)

    validateAssignPackageCommand(
        warehouseIndex = warehouseIndex,
        warehouseRepository = warehouseRepository,
        assignPackageToCargoQueueUseCase =
            assignPackageToCargoQueueUseCase
    )

    validateReroutePackageCommand(
        warehouseIndex = warehouseIndex,
        warehouseRepository = warehouseRepository,
        reroutePackageUseCase = reroutePackageUseCase
    )

    validateDispatchVehicleCommand(
        warehouseIndex = warehouseIndex,
        warehouseRepository = warehouseRepository,
        vehicleRepository = vehicleRepository,
        dispatchVehicleUseCase = dispatchVehicleUseCase
    )
    println("Command pattern validation completed.")
}

private fun validateAssignPackageCommand(
    warehouseIndex: Map<String, Warehouse>,
    warehouseRepository: WarehouseRepository,
    assignPackageToCargoQueueUseCase: AssignPackageToCargoQueueUseCase
) {
    val warehouse = warehouseIndex.values.firstOrNull()

    if (warehouse == null) {
        println("Assign Package Command validation skipped.")
        return
    }

    val originalQueue = warehouse.cargoQueue.toList()

    val originalPackage = warehouse.cargoQueue.firstOrNull()

    if (originalPackage == null) {
        println("Assign Package Command validation skipped.")
        return
    }

    val packageToAssign =
        originalPackage.copy(id = "${originalPackage.id}-command-test")

    val command = AssignPackageToQueueCommand(
            logisticsPackage = packageToAssign,
            dependencies = AssignPackageToQueueCommandDependencies(
            assignPackageToCargoQueueUseCase = assignPackageToCargoQueueUseCase,
            warehouseRepository = warehouseRepository)
        )

    val invoker = CommandInvoker()
    invoker.execute(command)

    val executed = warehouse.cargoQueue.size == originalQueue.size + 1
    invoker.undo()

    val undone = warehouse.cargoQueue == originalQueue

    invoker.redo()

    val redone =
        warehouse.cargoQueue == executedQueue

    invoker.undo()

    printCommandResult(
        commandName = "Assign Package Command",
        executed = executed,
        undone = undone)
}

private fun validateReroutePackageCommand(
    warehouseIndex: Map<String, Warehouse>,
    warehouseRepository: WarehouseRepository,
    reroutePackageUseCase: ReroutePackageUseCase
) {
    val warehouse = warehouseIndex.values
            .firstOrNull { it.cargoQueue.isNotEmpty() }

    val destinationWarehouse = warehouseIndex.values
            .firstOrNull { it.id != warehouse?.id }

    if (warehouse == null || destinationWarehouse == null) {
        println("Reroute Package Command validation skipped.")
        return
    }

    val packageToReroute = warehouse.cargoQueue.first()

    val originalDestination =
        packageToReroute.destinationWarehouse

    val command = ReroutePackageCommand(
            dependencies = ReroutePackageCommandDependencies(
                    reroutePackageUseCase = reroutePackageUseCase,
                    warehouseRepository = warehouseRepository),

            input = ReroutePackageRequest(
                    warehouseId = warehouse.id,
                    packageId = packageToReroute.id,
                    newDestinationWarehouse = destinationWarehouse
                )
        )

    val invoker = CommandInvoker()

    invoker.execute(command)

    val updatedPackage = warehouse.cargoQueue
            .firstOrNull { it.id == packageToReroute.id }

    val executed = updatedPackage?.destinationWarehouse == destinationWarehouse

    invoker.undo()

    val restoredPackage = warehouse.cargoQueue
            .firstOrNull { it.id == packageToReroute.id }

    val undone = restoredPackage?.destinationWarehouse == originalDestination

    printCommandResult(commandName = "Reroute Package Command",
        executed = executed,
        undone = undone,
        redone = redone
    )
}

private fun validateDispatchVehicleCommand(
    warehouseIndex: Map<String, Warehouse>,
    warehouseRepository: WarehouseRepository,
    vehicleRepository: VehicleRepository,
    dispatchVehicleUseCase: DispatchVehicleUseCase
) {
    val warehouse = warehouseIndex.values
            .firstOrNull {it.cargoQueue.isNotEmpty() &&
                        it.stationedVehicles.isNotEmpty() }

    if (warehouse == null) {
        println("Dispatch Vehicle Command validation skipped.")
        return
    }

    val vehicle = warehouse.stationedVehicles.first()

    val originalPackages = vehicle.assignedPackages.toList()

    val originalQueue = warehouse.cargoQueue.toList()

    val request = DispatchVehicleRequest(
            vehicleId = vehicle.id,
            warehouseId = warehouse.id)

    val command = DispatchVehicleCommand(
            dependencies = DispatchVehicleCommandDependencies(
                    dispatchVehicleUseCase = dispatchVehicleUseCase,
                    vehicleRepository = vehicleRepository,
                    warehouseRepository = warehouseRepository),

            request = request
        )

    val invoker = CommandInvoker()

    invoker.execute(command)

    val executed = vehicle.assignedPackages != originalPackages ||
                warehouse.cargoQueue != originalQueue

    invoker.undo()

    val undone = vehicle.assignedPackages == originalPackages &&
                warehouse.cargoQueue == originalQueue

    printCommandResult(commandName = "Dispatch Vehicle Command",
        executed = executed,
        undone = undone,
        redone = redone
    )
}

private fun printCommandResult(
    commandName: String,
    executed: Boolean,
    undone: Boolean,
    redone: Boolean
) {
    println(commandName)
    println("Execute:" + if (executed) "Passed" else "Failed")

    println("Undo:" + if (undone) "Passed" else "Failed")
}
