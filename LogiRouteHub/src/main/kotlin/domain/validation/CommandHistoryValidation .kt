package domain.validation

import domain.command.*
import domain.command.telemetry.CommandTelemetry
import domain.command.telemetry.ConsoleTelemetry
import domain.model.Package
import domain.model.Vehicle
import domain.model.Warehouse
import domain.model.request.DispatchVehicleRequest
import domain.model.request.ReroutePackageRequest
import domain.repository.PackageRepository
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository
import domain.usecase.AssignPackageToCargoQueueUseCase
import domain.usecase.DispatchVehicleUseCase
import domain.usecase.ReroutePackageUseCase
import domain.usecase.SortingCargoQueueUseCase

data class CommandHistory(
    val assignPackageToQueueCommand: Command,
    val dispatchVehicleCommand: Command,
    val reroutePackageCommand: Command
)

private data class HistoryValidationResult(
    val multiLevelUndoPassed: Boolean,
    val multiLevelRedoPassed: Boolean,
    val redoClearancePassed: Boolean,
    val finalRestorationPassed: Boolean
)

private data class HistoryValidationData(
    val warehouse: Warehouse,
    val destinationWarehouse: Warehouse,
    val vehicle: Vehicle,
    val packageToUse: Package
)

private class HistoryValidationWarehouseRepository(
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

    override fun add(warehouse: Warehouse) {
        if (warehouseIndex is MutableMap) {
            warehouseIndex[warehouse.id] = warehouse
        }
    }

    override fun update(warehouse: Warehouse) {
        if (warehouseIndex is MutableMap) {
            warehouseIndex[warehouse.id] = warehouse
        }
    }
}

private class HistoryValidationVehicleRepository(
    private val warehouseIndex: Map<String, Warehouse>
) : VehicleRepository {

    override fun findById(
        vehicleId: String
    ): Vehicle? {
        return warehouseIndex.values
            .flatMap { it.stationedVehicles }
            .firstOrNull { it.id == vehicleId }
    }

    override fun findAll(): List<Vehicle> {
        return warehouseIndex.values.flatMap { it.stationedVehicles }
    }

    override fun add(vehicle: Vehicle) {
        val warehouse = warehouseIndex[vehicle.currentWarehouse.id]
        if (warehouse != null) {
            val currentVehicles = warehouse.stationedVehicles as? MutableList<Vehicle>
            currentVehicles?.add(vehicle)
        }
    }

    override fun update(vehicle: Vehicle) {
        val warehouse = warehouseIndex[vehicle.currentWarehouse.id]
        val vehicles = warehouse?.stationedVehicles as? MutableList<Vehicle>
        val index = vehicles?.indexOfFirst { it.id == vehicle.id } ?: -1
        if (index != -1 && vehicles != null) {
            vehicles[index] = vehicle
        }
    }
}

private class HistoryValidationPackageRepository(
    private val warehouseIndex: Map<String, Warehouse>
) : PackageRepository {

    override fun findById(packageId: String): Package? {
        return warehouseIndex.values
            .flatMap { it.cargoQueue }
            .firstOrNull { it.id == packageId }
    }

    override fun findAll(): List<Package> {
        return warehouseIndex.values.flatMap { it.cargoQueue }
    }

    override fun add(packageItem: Package) {
        val warehouse = warehouseIndex[packageItem.originWarehouse.id]
        if (warehouse != null) {
            val queue = warehouse.cargoQueue as? MutableList<Package>
            queue?.add(packageItem)
        }
    }

    override fun update(packageItem: Package) {
        warehouseIndex.values.forEach { warehouse ->
            val queue = warehouse.cargoQueue as? MutableList<Package>
            val index = queue?.indexOfFirst { it.id == packageItem.id } ?: -1
            if (index != -1 && queue != null) {
                queue[index] = packageItem
            }
        }
    }
}

fun validateCommandHistory(
    warehouseIndex: Map<String, Warehouse>
) {
    if (warehouseIndex.isEmpty()) {
        printHistoryValidationSkipped("No warehouses were built.")
        return
    }
    val telemetry = ConsoleTelemetry()

    val history = buildCommandHistory(
        warehouseIndex = warehouseIndex,
        telemetry = telemetry
    )

    if (history == null) {
        printHistoryValidationSkipped("Suitable validation data was not found.")
        return
    }

    CommandHistoryValidation().run(
        history = history,
        stateSnapshot = { createHistorySnapshot(warehouseIndex) },
        telemetry = telemetry
    )
}

private fun buildCommandHistory(
    warehouseIndex: Map<String, Warehouse>,
    telemetry: CommandTelemetry
): CommandHistory? {

    val validationData = findHistoryValidationData(warehouseIndex)
        ?: return null

    val warehouseRepository = HistoryValidationWarehouseRepository(
        warehouseIndex
    )

    val vehicleRepository = HistoryValidationVehicleRepository(
        warehouseIndex
    )

    val packageRepository = HistoryValidationPackageRepository(
        warehouseIndex
    )

    return createCommandHistory(
        validationData,
        warehouseRepository,
        vehicleRepository,
        packageRepository,
        telemetry
    )
}

private fun createCommandHistory(
    data: HistoryValidationData,
    warehouseRepository: WarehouseRepository,
    vehicleRepository: VehicleRepository,
    packageRepository: PackageRepository,
    telemetry: CommandTelemetry
): CommandHistory {

    return CommandHistory(
        assignPackageToQueueCommand = createHistoryAssignCommand(
            data,
            warehouseRepository,
            telemetry
        ),
        dispatchVehicleCommand = createHistoryDispatchCommand(
            data,
            warehouseRepository,
            vehicleRepository,
            telemetry
        ),
        reroutePackageCommand = createHistoryRerouteCommand(
            data,
            warehouseRepository,
            packageRepository,
            telemetry
        )
    )
}

private fun findHistoryValidationData(
    warehouseIndex: Map<String, Warehouse>
): HistoryValidationData? {

    val warehouse = findWarehouseForHistoryValidation(
        warehouseIndex
    )
        ?: return null

    val destinationWarehouse = warehouseIndex.values.firstOrNull {
        it.id != warehouse.id
    }
        ?: return null

    val vehicle = findSuitableVehicle(warehouse) ?: return null

    val logisticsPackage = warehouse.cargoQueue.firstOrNull() ?: return null

    return HistoryValidationData(
        warehouse = warehouse,
        destinationWarehouse = destinationWarehouse,
        vehicle = vehicle,
        packageToUse = logisticsPackage
    )
}

private fun findWarehouseForHistoryValidation(
    warehouseIndex: Map<String, Warehouse>
): Warehouse? {

    return warehouseIndex.values.firstOrNull { warehouse ->
        warehouse.cargoQueue.isNotEmpty() && warehouse.stationedVehicles.isNotEmpty() &&
                findSuitableVehicle(warehouse) != null
    }
}

private fun findSuitableVehicle(
    warehouse: Warehouse
): Vehicle? {

    return warehouse.stationedVehicles.firstOrNull { vehicle ->

        val availableCapacity = vehicle.maxCapacityKg - vehicle.calculateTotalLoadedWeightKg()

        warehouse.cargoQueue.any { logisticsPackage ->
            logisticsPackage.weightByKg <= availableCapacity
        }
    }
}

private fun createHistoryAssignCommand(
    data: HistoryValidationData,
    warehouseRepository: WarehouseRepository,
    telemetry: CommandTelemetry
): AssignPackageToQueueCommand {

    val useCase = AssignPackageToCargoQueueUseCase(
        sortCargoQueue = SortingCargoQueueUseCase(),
        warehouseRepository = warehouseRepository
    )

    val packageToAssign = data.packageToUse.copy(
        id = "${data.packageToUse.id}-history-test"
    )

    return AssignPackageToQueueCommand(
        logisticsPackage = packageToAssign,
        dependencies = AssignPackageToQueueCommandDependencies(
            assignPackageToCargoQueueUseCase = useCase,
            warehouseRepository = warehouseRepository,
            telemetry = telemetry
        )
    )
}

private fun createHistoryRerouteCommand(
    data: HistoryValidationData,
    warehouseRepository: WarehouseRepository,
    packageRepository: PackageRepository,
    telemetry: CommandTelemetry
): ReroutePackageCommand {

    val useCase = ReroutePackageUseCase(
        warehouseRepository = warehouseRepository,
        packageRepository = packageRepository
    )
    val request = ReroutePackageRequest(
        warehouseId = data.warehouse.id,
        packageId = data.packageToUse.id,
        newDestinationWarehouse = data.destinationWarehouse
    )

    return ReroutePackageCommand(
        dependencies = ReroutePackageCommandDependencies(
            reroutePackageUseCase = useCase,
            packageRepository = packageRepository,
            warehouseRepository = warehouseRepository,
            telemetry = telemetry
        ),
        input = request
    )
}

private fun createHistoryDispatchCommand(
    data: HistoryValidationData,
    warehouseRepository: WarehouseRepository,
    vehicleRepository: VehicleRepository,
    telemetry: CommandTelemetry
): DispatchVehicleCommand {

    val useCase = DispatchVehicleUseCase(
        vehicleRepository = vehicleRepository,
        warehouseRepository = warehouseRepository
    )
    val request = DispatchVehicleRequest(
        vehicleId = data.vehicle.id,
        warehouseId = data.warehouse.id
    )

    return DispatchVehicleCommand(
        dependencies = DispatchVehicleCommandDependencies(
            dispatchVehicleUseCase = useCase,
            vehicleRepository = vehicleRepository,
            warehouseRepository = warehouseRepository,
            telemetry = telemetry
        ),
        request = request
    )
}

class CommandHistoryValidation {

    fun run(
        history: CommandHistory,
        stateSnapshot: () -> String,
        telemetry: CommandTelemetry
    ) {
        printHeader()

        val invoker = CommandInvoker(telemetry)
        val result = validateHistory(
            history = history,
            invoker = invoker,
            stateSnapshot = stateSnapshot
        )
        printResult(result)
    }

    private fun validateHistory(
        history: CommandHistory,
        invoker: CommandInvoker,
        stateSnapshot: () -> String
    ): HistoryValidationResult {

        val initialState = stateSnapshot()

        val forwardStates = executeInitialCommands(
            history,
            invoker,
            stateSnapshot
        )

        val undoPassed = validateMultiLevelUndo(
            invoker,
            stateSnapshot,
            initialState,
            forwardStates.first
        )
        return continueHistoryValidation(
            history,
            invoker,
            stateSnapshot,
            initialState,
            forwardStates,
            undoPassed
        )
    }

    private fun continueHistoryValidation(
        history: CommandHistory,
        invoker: CommandInvoker,
        stateSnapshot: () -> String,
        initialState: String,
        forwardStates: Pair<String, String>,
        undoPassed: Boolean
    ): HistoryValidationResult {

        val redoPassed = validateMultiLevelRedo(
            invoker,
            stateSnapshot,
            forwardStates
        )

        val clearanceResult = validateRedoClearance(
            history,
            invoker,
            stateSnapshot,
            forwardStates.first
        )

        return HistoryValidationResult(
            multiLevelUndoPassed = undoPassed,
            multiLevelRedoPassed = redoPassed,
            redoClearancePassed = clearanceResult.first,
            finalRestorationPassed = clearanceResult.second == initialState
        )
    }

    private fun executeInitialCommands(
        history: CommandHistory,
        invoker: CommandInvoker,
        stateSnapshot: () -> String
    ): Pair<String, String> {

        printScenario("Execute Assign -> Execute Reroute")

        invoker.execute(history.assignPackageToQueueCommand)

        val afterAssign = stateSnapshot()

        invoker.execute(history.reroutePackageCommand)

        val afterReroute = stateSnapshot()

        return Pair(
            afterAssign,
            afterReroute
        )
    }

    private fun validateMultiLevelUndo(
        invoker: CommandInvoker,
        stateSnapshot: () -> String,
        initialState: String,
        afterAssign: String
    ): Boolean {

        printScenario("Multi-Level Undo")

        invoker.undo()

        val firstUndoPassed = stateSnapshot() == afterAssign

        invoker.undo()

        val secondUndoPassed = stateSnapshot() == initialState

        return firstUndoPassed && secondUndoPassed
    }

    private fun validateMultiLevelRedo(
        invoker: CommandInvoker,
        stateSnapshot: () -> String,
        forwardStates: Pair<String, String>
    ): Boolean {

        printScenario("Multi-Level Redo")

        invoker.redo()

        val firstRedoPassed = stateSnapshot() == forwardStates.first

        invoker.redo()

        val secondRedoPassed = stateSnapshot() == forwardStates.second

        return firstRedoPassed && secondRedoPassed
    }

    private fun validateRedoClearance(
        history: CommandHistory,
        invoker: CommandInvoker,
        stateSnapshot: () -> String,
        afterAssign: String
    ): Pair<Boolean, String> {

        printScenario("Redo Stack Clearance")
        invoker.undo()

        val undoPassed = stateSnapshot() == afterAssign

        invoker.execute(history.dispatchVehicleCommand)
        val afterDispatch = stateSnapshot()

        invoker.redo()

        val redoWasCleared = stateSnapshot() == afterDispatch

        val finalState = restoreInitialState(invoker, stateSnapshot)

        return Pair(undoPassed && redoWasCleared, finalState)
    }

    private fun restoreInitialState(
        invoker: CommandInvoker,
        stateSnapshot: () -> String
    ): String {

        printScenario("Restore Initial State")

        invoker.undo()
        invoker.undo()

        return stateSnapshot()
    }

    private fun printHeader() {
        println()
        println("============================================================")
        println("              COMMAND HISTORY VALIDATION")
        println("============================================================")
    }

    private fun printScenario(
        title: String
    ) {
        println()
        println("------------------------------------------------------------")
        println("SCENARIO: $title")
        println("------------------------------------------------------------")
    }

    private fun printResult(
        result: HistoryValidationResult
    ) {
        println()
        println("============================================================")
        println(
            "                 HISTORY TEST RESULTS"
        )
        println("============================================================")

        printValidationChecks(result)

        println("============================================================")
    }

    private fun printValidationChecks(
        result: HistoryValidationResult
    ) {
        printCheck("Multi-Level Undo", result.multiLevelUndoPassed)

        printCheck("Multi-Level Redo", result.multiLevelRedoPassed)

        printCheck("Redo Stack Clearance", result.redoClearancePassed)

        printCheck("Final State Restoration", result.finalRestorationPassed)
    }

    private fun printCheck(
        testName: String,
        passed: Boolean
    ) {
        val result = if (passed) {
            "[PASSED]"
        } else {
            "[FAILED]"
        }

        println("  ${testName.padEnd(25)} $result")
    }
}

private fun createHistorySnapshot(
    warehouseIndex: Map<String, Warehouse>
): String {

    return buildString {
        warehouseIndex.values.sortedBy { it.id }
            .forEach { warehouse ->
                appendWarehouseSnapshot(
                    warehouse
                )
            }
    }
}

private fun StringBuilder.appendWarehouseSnapshot(
    warehouse: Warehouse
) {
    append("Warehouse=${warehouse.id}")

    append("|Queue=${warehouse.cargoQueue.map { it.id }}")

    append("|Vehicles=${createVehicleSnapshot(warehouse)}")

    appendLine()
}

private fun createVehicleSnapshot(
    warehouse: Warehouse
): List<Pair<String, List<String>>> {

    return warehouse.stationedVehicles.map { vehicle ->

        Pair(vehicle.id, vehicle.assignedPackages.map { logisticsPackage -> logisticsPackage.id }
        )
    }
}

private fun printHistoryValidationSkipped(
    reason: String
) {
    println()
    println("============================================================")
    println("              COMMAND HISTORY VALIDATION              ")
    println("============================================================")
    println("[SKIPPED] $reason")
    println("============================================================")
}
