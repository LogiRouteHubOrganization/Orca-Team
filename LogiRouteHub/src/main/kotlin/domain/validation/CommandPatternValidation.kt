package domain.validation

import domain.command.*
import domain.command.telemetry.ConsoleTelemetry
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


private data class CommandValidationResult(
    val executed: Boolean,
    val undone: Boolean,
    val redone: Boolean
)

private class ValidationWarehouseRepository(
    private val warehouseIndex: Map<String, Warehouse>
) : WarehouseRepository {

    override fun findById(
        warehouseId: String
    ): Warehouse? {
        return warehouseIndex[warehouseId]
    }

    override fun findAll(): List<Warehouse> {
        return warehouseIndex.values.toList()
    }

    override fun update(
        warehouse: Warehouse
    ) {
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

    override fun findById(
        vehicleId: String
    ): Vehicle? {
        return warehouseIndex.values.flatMap { warehouse -> warehouse.stationedVehicles }
            .firstOrNull { vehicle -> vehicle.id == vehicleId }
    }

    override fun update(vehicle: Vehicle) {
    }
}

fun validateCommandPattern(
    warehouseIndex: Map<String, Warehouse>
) {
    printValidationSuiteHeader()

    if (warehouseIndex.isEmpty()) {
        printEmptyValidationMessage()
        return
    }

    val warehouseRepository = ValidationWarehouseRepository(warehouseIndex)

    val vehicleRepository = ValidationVehicleRepository(warehouseIndex)

    val telemetry = ConsoleTelemetry()

    runCommandValidations(
        warehouseIndex = warehouseIndex,
        warehouseRepository = warehouseRepository,
        vehicleRepository = vehicleRepository,
        telemetry = telemetry
    )
    printValidationSuiteFooter()
}

private fun runCommandValidations(
    warehouseIndex: Map<String, Warehouse>,
    warehouseRepository: WarehouseRepository,
    vehicleRepository: VehicleRepository,
    telemetry: ConsoleTelemetry
) {
    val assignUseCase = createAssignPackageUseCase(warehouseRepository)

    val dispatchUseCase = createDispatchVehicleUseCase(warehouseRepository, vehicleRepository)

    val rerouteUseCase = ReroutePackageUseCase(
        warehouseRepository = warehouseRepository
    )

    validateAllCommands(
        warehouseIndex,
        warehouseRepository,
        vehicleRepository,
        assignUseCase,
        dispatchUseCase,
        rerouteUseCase,
        telemetry
    )
}

private fun validateAllCommands(
    warehouseIndex: Map<String, Warehouse>,
    warehouseRepository: WarehouseRepository,
    vehicleRepository: VehicleRepository,
    assignUseCase: AssignPackageToCargoQueueUseCase,
    dispatchUseCase: DispatchVehicleUseCase,
    rerouteUseCase: ReroutePackageUseCase,
    telemetry: ConsoleTelemetry
) {
    validateAssignPackageCommand(
        warehouseIndex,
        warehouseRepository,
        assignUseCase,
        telemetry
    )

    validateReroutePackageCommand(
        warehouseIndex,
        warehouseRepository,
        rerouteUseCase,
        telemetry
    )

    validateDispatchVehicleCommand(
        warehouseIndex,
        warehouseRepository,
        vehicleRepository,
        dispatchUseCase,
        telemetry
    )
}

private fun createAssignPackageUseCase(
    warehouseRepository: WarehouseRepository
): AssignPackageToCargoQueueUseCase {

    return AssignPackageToCargoQueueUseCase(
        sortCargoQueue = SortingCargoQueueUseCase(),
        warehouseRepository = warehouseRepository
    )
}


private fun createDispatchVehicleUseCase(
    warehouseRepository: WarehouseRepository,
    vehicleRepository: VehicleRepository
): DispatchVehicleUseCase {

    return DispatchVehicleUseCase(
        vehicleRepository = vehicleRepository,
        warehouseRepository = warehouseRepository
    )
}


private fun validateAssignPackageCommand(
    warehouseIndex: Map<String, Warehouse>,
    warehouseRepository: WarehouseRepository,
    assignUseCase: AssignPackageToCargoQueueUseCase,
    telemetry: ConsoleTelemetry
) {
    val warehouse = warehouseIndex.values.firstOrNull()

    val originalPackage = warehouse?.cargoQueue?.firstOrNull()

    if (warehouse == null || originalPackage == null) {
        printValidationSkipped("Assign Package Command")
        return
    }

    val command = createAssignPackageCommand(
        originalPackage,
        warehouseRepository,
        assignUseCase,
        telemetry
    )

    validateAssignExecution(
        warehouse,
        command,
        telemetry
    )
}

private fun createAssignPackageCommand(
    originalPackage: Package,
    warehouseRepository: WarehouseRepository,
    assignUseCase: AssignPackageToCargoQueueUseCase,
    telemetry: ConsoleTelemetry
): AssignPackageToQueueCommand {

    val packageToAssign = originalPackage.copy(
        id = "${originalPackage.id}-command-test"
    )

    val dependencies = AssignPackageToQueueCommandDependencies(
        assignPackageToCargoQueueUseCase = assignUseCase,
        warehouseRepository = warehouseRepository,
        telemetry = telemetry
    )

    return AssignPackageToQueueCommand(
        logisticsPackage = packageToAssign,
        dependencies = dependencies
    )
}


private fun validateAssignExecution(
    warehouse: Warehouse,
    command: AssignPackageToQueueCommand,
    telemetry: ConsoleTelemetry
) {
    printValidationHeader("Assign Package Command")

    val originalQueue = warehouse.cargoQueue.toList()

    val invoker = CommandInvoker(telemetry)

    val result = testAssignHistory(
        warehouse,
        originalQueue,
        command,
        invoker
    )
    printCommandResult("Assign Package Command", result)
}


private fun testAssignHistory(
    warehouse: Warehouse,
    originalQueue: List<Package>,
    command: AssignPackageToQueueCommand,
    invoker: CommandInvoker
): CommandValidationResult {

    invoker.execute(command)

    val executedQueue = warehouse.cargoQueue.toList()

    val executed = executedQueue.size == originalQueue.size + 1

    invoker.undo()
    val undone = warehouse.cargoQueue == originalQueue

    invoker.redo()
    val redone = warehouse.cargoQueue == executedQueue

    invoker.undo()

    return CommandValidationResult(
        executed,
        undone,
        redone
    )
}

private fun validateReroutePackageCommand(
    warehouseIndex: Map<String, Warehouse>,
    warehouseRepository: WarehouseRepository,
    rerouteUseCase: ReroutePackageUseCase,
    telemetry: ConsoleTelemetry
) {
    val validationData = findRerouteValidationData(warehouseIndex)

    if (validationData == null) {
        printValidationSkipped("Reroute Package Command")
        return
    }

    val command = createReroutePackageCommand(
        validationData.first,
        validationData.second,
        warehouseRepository,
        rerouteUseCase,
        telemetry
    )

    validateRerouteExecution(
        validationData.first,
        validationData.second,
        command,
        telemetry
    )
}


private fun findRerouteValidationData(
    warehouseIndex: Map<String, Warehouse>
): Pair<Warehouse, Warehouse>? {

    val warehouse = warehouseIndex.values.firstOrNull { it.cargoQueue.isNotEmpty() } ?: return null

    val destinationWarehouse = warehouseIndex.values.firstOrNull { it.id != warehouse.id } ?: return null

    return Pair(
        warehouse,
        destinationWarehouse
    )
}

private fun createReroutePackageCommand(
    warehouse: Warehouse,
    destinationWarehouse: Warehouse,
    warehouseRepository: WarehouseRepository,
    rerouteUseCase: ReroutePackageUseCase,
    telemetry: ConsoleTelemetry
): ReroutePackageCommand {

    val packageToReroute = warehouse.cargoQueue.first()

    val request = ReroutePackageRequest(
        warehouseId = warehouse.id,
        packageId = packageToReroute.id,
        newDestinationWarehouse = destinationWarehouse
    )

    return ReroutePackageCommand(
        dependencies = createRerouteDependencies(
            warehouseRepository,
            rerouteUseCase,
            telemetry
        ),
        input = request
    )
}

private fun createRerouteDependencies(
    warehouseRepository: WarehouseRepository,
    rerouteUseCase: ReroutePackageUseCase,
    telemetry: ConsoleTelemetry
): ReroutePackageCommandDependencies {

    return ReroutePackageCommandDependencies(
        reroutePackageUseCase = rerouteUseCase,
        warehouseRepository = warehouseRepository,
        telemetry = telemetry
    )
}

private fun validateRerouteExecution(
    warehouse: Warehouse,
    destinationWarehouse: Warehouse,
    command: ReroutePackageCommand,
    telemetry: ConsoleTelemetry
) {
    printValidationHeader("Reroute Package Command")
    val packageToReroute = warehouse.cargoQueue.first()

    val originalDestination = packageToReroute.destinationWarehouse

    val invoker = CommandInvoker(telemetry)

    val result = testRerouteHistory(
        warehouse,
        packageToReroute.id,
        originalDestination,
        destinationWarehouse,
        command,
        invoker
    )
    printCommandResult("Reroute Package Command", result)
}

private fun testRerouteHistory(
    warehouse: Warehouse,
    packageId: String,
    originalDestination: Warehouse,
    newDestination: Warehouse,
    command: ReroutePackageCommand,
    invoker: CommandInvoker
): CommandValidationResult {

    invoker.execute(command)

    val executed = hasPackageDestination(
        warehouse,
        packageId,
        newDestination
    )

    invoker.undo()

    val undone = hasPackageDestination(
        warehouse,
        packageId,
        originalDestination
    )
    invoker.redo()

    val redone = hasPackageDestination(
        warehouse,
        packageId,
        newDestination
    )
    invoker.undo()
    return CommandValidationResult(
        executed,
        undone,
        redone
    )
}

private fun hasPackageDestination(
    warehouse: Warehouse,
    packageId: String,
    destinationWarehouse: Warehouse
): Boolean {

    val logisticsPackage = warehouse.cargoQueue.firstOrNull { it.id == packageId }

    return logisticsPackage?.destinationWarehouse == destinationWarehouse
}

private fun validateDispatchVehicleCommand(
    warehouseIndex: Map<String, Warehouse>,
    warehouseRepository: WarehouseRepository,
    vehicleRepository: VehicleRepository,
    dispatchUseCase: DispatchVehicleUseCase,
    telemetry: ConsoleTelemetry
) {
    val warehouse = findWarehouseForDispatchValidation(warehouseIndex)

    if (warehouse == null) {
        printValidationSkipped("Dispatch Vehicle Command")
        return
    }

    val vehicle = warehouse.stationedVehicles.first()

    val command = createDispatchVehicleCommand(
        warehouse,
        vehicle,
        warehouseRepository,
        vehicleRepository,
        dispatchUseCase,
        telemetry
    )

    validateDispatchExecution(
        warehouse,
        vehicle,
        command,
        telemetry
    )
}

private fun findWarehouseForDispatchValidation(
    warehouseIndex: Map<String, Warehouse>
): Warehouse? {

    return warehouseIndex.values
        .firstOrNull { it.cargoQueue.isNotEmpty() && it.stationedVehicles.isNotEmpty() }
}


private fun createDispatchVehicleCommand(
    warehouse: Warehouse,
    vehicle: Vehicle,
    warehouseRepository: WarehouseRepository,
    vehicleRepository: VehicleRepository,
    dispatchUseCase: DispatchVehicleUseCase,
    telemetry: ConsoleTelemetry
): DispatchVehicleCommand {

    val request = DispatchVehicleRequest(
        vehicleId = vehicle.id,
        warehouseId = warehouse.id
    )

    val dependencies = DispatchVehicleCommandDependencies(
        dispatchVehicleUseCase = dispatchUseCase,
        vehicleRepository = vehicleRepository,
        warehouseRepository = warehouseRepository,
        telemetry = telemetry
    )

    return DispatchVehicleCommand(
        dependencies = dependencies,
        request = request
    )
}


private fun validateDispatchExecution(
    warehouse: Warehouse,
    vehicle: Vehicle,
    command: DispatchVehicleCommand,
    telemetry: ConsoleTelemetry
) {
    printValidationHeader(
        "Dispatch Vehicle Command"
    )

    val originalPackages = vehicle.assignedPackages.toList()

    val originalQueue = warehouse.cargoQueue.toList()

    val invoker = CommandInvoker(telemetry)

    invoker.execute(command)

    val executedPackages = vehicle.assignedPackages.toList()

    val executedQueue = warehouse.cargoQueue.toList()

    val result = testDispatchHistory(
        warehouse,
        vehicle,
        originalPackages,
        originalQueue,
        executedPackages,
        executedQueue,
        invoker
    )

    printCommandResult("Dispatch Vehicle Command", result)
}

private fun testDispatchHistory(
    warehouse: Warehouse,
    vehicle: Vehicle,
    originalPackages: List<Package>,
    originalQueue: List<Package>,
    executedPackages: List<Package>,
    executedQueue: List<Package>,
    invoker: CommandInvoker
): CommandValidationResult {

    val executed = vehicle.assignedPackages != originalPackages || warehouse.cargoQueue != originalQueue

    invoker.undo()

    val undone = vehicle.assignedPackages == originalPackages && warehouse.cargoQueue == originalQueue

    invoker.redo()

    val redone = vehicle.assignedPackages == executedPackages && warehouse.cargoQueue == executedQueue

    invoker.undo()
    return CommandValidationResult(
        executed,
        undone,
        redone
    )
}

private fun printValidationSuiteHeader() {
    println()
    println("============================================================")
    println("              COMMAND PATTERN VALIDATION")
    println("============================================================")
}

private fun printValidationSuiteFooter() {
    println()
    println("============================================================")
    println("                 VALIDATION COMPLETED")
    println("============================================================")
}

private fun printValidationHeader(
    commandName: String
) {
    println()
    println("------------------------------------------------------------")
    println("TEST: $commandName")
    println("------------------------------------------------------------")
}

private fun printValidationSkipped(
    commandName: String
) {
    println()
    println("------------------------------------------------------------")
    println("TEST: $commandName")
    println("RESULT: [SKIPPED]")
    println("------------------------------------------------------------")
}

private fun printEmptyValidationMessage() {
    println()
    println("[SKIPPED] No warehouses were built.")
    println()
}

private fun printCommandResult(
    commandName: String,
    result: CommandValidationResult
) {
    println()
    println("RESULT: $commandName")
    println(
        "  Execute : ${getResultText(result.executed)}"
    )
    println(
        "  Undo    : ${getResultText(result.undone)}"
    )
    println(
        "  Redo    : ${getResultText(result.redone)}"
    )
    println("------------------------------------------------------------")
}


private fun getResultText(
    passed: Boolean
): String {
    return if (passed) {
        "[PASSED]"
    } else {
        "[FAILED]"
    }
}
