package command

import data.repository.memory.InMemoryVehicleRepository
import data.repository.memory.InMemoryWarehouseRepository
import domain.algorithm.sorting.PackageQuickSorter
import domain.command.AssignPackageToCargoQueueCommand
import domain.command.AssignPackageToCargoQueueCommandDependencies
import domain.command.CommandInvoker
import domain.command.DispatchVehicleCommand
import domain.command.DispatchVehicleCommandDependencies
import domain.model.Package
import domain.model.Vehicle
import domain.model.Warehouse
import domain.model.request.DispatchVehicleRequest
import domain.usecase.AssignPackageToCargoQueueUseCase
import domain.usecase.DispatchVehicleUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommandInvokerTest {

    // ─────────────────────────────────────────────
    // Test fixtures
    // ─────────────────────────────────────────────

    private fun buildWarehouse(id: String = "WH-1"): Warehouse =
        Warehouse(
            id = id,
            name = "Test Warehouse",
            regionalZone = "Zone-A",
            latitude = 0.0,
            longitude = 0.0
        )

    private fun buildVehicle(
        id: String = "V-1",
        maxCapacityKg: Double = 100.0,
        warehouse: Warehouse
    ): Vehicle =
        Vehicle(
            id = id,
            maxCapacityKg = maxCapacityKg,
            costPerKm = 1.5,
            currentWarehouse = warehouse
        )

    private fun buildPackage(
        id: String,
        weightByKg: Double,
        origin: Warehouse,
        destination: Warehouse
    ): Package =
        Package(
            id = id,
            weightByKg = weightByKg,
            priority = "STANDARD",
            originWarehouse = origin,
            destinationWarehouse = destination
        )

    // ─────────────────────────────────────────────
    // Wiring helpers
    // ─────────────────────────────────────────────

    private fun buildDispatchDependencies(
        vehicles: MutableList<Vehicle>,
        warehouses: MutableList<Warehouse>
    ): DispatchVehicleCommandDependencies {
        val vehicleRepo = InMemoryVehicleRepository(vehicles)
        val warehouseRepo = InMemoryWarehouseRepository(warehouses)
        val useCase = DispatchVehicleUseCase(vehicleRepo, warehouseRepo)
        return DispatchVehicleCommandDependencies(useCase, vehicleRepo, warehouseRepo)
    }

    private fun buildAssignDependencies(
        warehouses: MutableList<Warehouse>
    ): AssignPackageToCargoQueueCommandDependencies {
        val warehouseRepo = InMemoryWarehouseRepository(warehouses)
        val useCase = AssignPackageToCargoQueueUseCase(PackageQuickSorter(), warehouseRepo)
        return AssignPackageToCargoQueueCommandDependencies(useCase, warehouseRepo)
    }

    // ═══════════════════════════════════════════════════════
    // AssignPackageToCargoQueueCommand Tests
    // ═══════════════════════════════════════════════════════

    @Test
    fun `AssignPackageCommand execute adds package to warehouse cargo queue`() {
        val origin = buildWarehouse("WH-1")
        val destination = buildWarehouse("WH-2")
        val pkg = buildPackage("PKG-001", 10.0, origin, destination)

        val deps = buildAssignDependencies(mutableListOf(origin, destination))
        val command = AssignPackageToCargoQueueCommand(deps, pkg)

        command.execute()

        assertEquals(1, origin.cargoQueue.size)
        assertEquals("PKG-001", origin.cargoQueue.first().id)
    }

    @Test
    fun `AssignPackageCommand execute returns true`() {
        val origin = buildWarehouse("WH-1")
        val destination = buildWarehouse("WH-2")
        val pkg = buildPackage("PKG-001", 10.0, origin, destination)

        val deps = buildAssignDependencies(mutableListOf(origin, destination))
        val command = AssignPackageToCargoQueueCommand(deps, pkg)

        val result = command.execute()

        assertTrue(result)
    }

    @Test
    fun `AssignPackageCommand undo removes package from warehouse cargo queue`() {
        val origin = buildWarehouse("WH-1")
        val destination = buildWarehouse("WH-2")
        val pkg = buildPackage("PKG-001", 10.0, origin, destination)

        val deps = buildAssignDependencies(mutableListOf(origin, destination))
        val command = AssignPackageToCargoQueueCommand(deps, pkg)

        command.execute()
        assertEquals(1, origin.cargoQueue.size)

        command.undo()

        assertEquals(0, origin.cargoQueue.size)
    }

    @Test
    fun `AssignPackageCommand sorts queue by descending weight after execute`() {
        val origin = buildWarehouse("WH-1")
        val destination = buildWarehouse("WH-2")

        val lightPkg = buildPackage("PKG-light", 5.0, origin, destination)
        val heavyPkg = buildPackage("PKG-heavy", 30.0, origin, destination)

        val deps = buildAssignDependencies(mutableListOf(origin, destination))

        AssignPackageToCargoQueueCommand(deps, lightPkg).execute()
        AssignPackageToCargoQueueCommand(deps, heavyPkg).execute()

        // QuickSorter sorts descending by weight — heaviest first
        assertEquals("PKG-heavy", origin.cargoQueue[0].id)
        assertEquals("PKG-light", origin.cargoQueue[1].id)
    }

    @Test
    fun `AssignPackageCommand undo restores previous queue order`() {
        val origin = buildWarehouse("WH-1")
        val destination = buildWarehouse("WH-2")

        val firstPkg = buildPackage("PKG-001", 10.0, origin, destination)
        val secondPkg = buildPackage("PKG-002", 5.0, origin, destination)

        val deps = buildAssignDependencies(mutableListOf(origin, destination))

        val firstCommand = AssignPackageToCargoQueueCommand(deps, firstPkg)
        val secondCommand = AssignPackageToCargoQueueCommand(deps, secondPkg)

        firstCommand.execute()
        secondCommand.execute()
        assertEquals(2, origin.cargoQueue.size)

        secondCommand.undo()

        assertEquals(1, origin.cargoQueue.size)
        assertEquals("PKG-001", origin.cargoQueue.first().id)
    }

    // ═══════════════════════════════════════════════════════
    // DispatchVehicleCommand Tests
    // ═══════════════════════════════════════════════════════

    @Test
    fun `DispatchVehicleCommand execute moves packages from warehouse to vehicle`() {
        val warehouse = buildWarehouse("WH-1")
        val destination = buildWarehouse("WH-2")
        val vehicle = buildVehicle("V-1", 50.0, warehouse)

        val pkg1 = buildPackage("PKG-001", 10.0, warehouse, destination)
        val pkg2 = buildPackage("PKG-002", 20.0, warehouse, destination)
        warehouse.addPackage(pkg1)
        warehouse.addPackage(pkg2)

        val deps = buildDispatchDependencies(
            mutableListOf(vehicle),
            mutableListOf(warehouse, destination)
        )
        val command = DispatchVehicleCommand(
            deps,
            DispatchVehicleRequest("V-1", "WH-1")
        )

        command.execute()

        assertEquals(0, warehouse.cargoQueue.size)
        assertEquals(2, vehicle.assignedPackages.size)
    }

    @Test
    fun `DispatchVehicleCommand execute returns true when packages are dispatched`() {
        val warehouse = buildWarehouse("WH-1")
        val destination = buildWarehouse("WH-2")
        val vehicle = buildVehicle("V-1", 50.0, warehouse)

        warehouse.addPackage(buildPackage("PKG-001", 10.0, warehouse, destination))

        val deps = buildDispatchDependencies(
            mutableListOf(vehicle),
            mutableListOf(warehouse, destination)
        )
        val command = DispatchVehicleCommand(
            deps,
            DispatchVehicleRequest("V-1", "WH-1")
        )

        val result = command.execute()

        assertTrue(result)
    }

    @Test
    fun `DispatchVehicleCommand execute returns false when vehicle not found`() {
        val warehouse = buildWarehouse("WH-1")
        val destination = buildWarehouse("WH-2")

        val deps = buildDispatchDependencies(
            mutableListOf(), // no vehicles
            mutableListOf(warehouse, destination)
        )
        val command = DispatchVehicleCommand(
            deps,
            DispatchVehicleRequest("V-MISSING", "WH-1")
        )

        val result = command.execute()

        assertEquals(false, result)
    }

    @Test
    fun `DispatchVehicleCommand execute returns false when warehouse is empty`() {
        val warehouse = buildWarehouse("WH-1")
        val vehicle = buildVehicle("V-1", 50.0, warehouse)
        // no packages added to warehouse

        val deps = buildDispatchDependencies(
            mutableListOf(vehicle),
            mutableListOf(warehouse)
        )
        val command = DispatchVehicleCommand(
            deps,
            DispatchVehicleRequest("V-1", "WH-1")
        )

        val result = command.execute()

        assertEquals(false, result)
    }

    @Test
    fun `DispatchVehicleCommand undo restores packages back to warehouse and clears vehicle`() {
        val warehouse = buildWarehouse("WH-1")
        val destination = buildWarehouse("WH-2")
        val vehicle = buildVehicle("V-1", 50.0, warehouse)

        val pkg1 = buildPackage("PKG-001", 10.0, warehouse, destination)
        val pkg2 = buildPackage("PKG-002", 20.0, warehouse, destination)
        warehouse.addPackage(pkg1)
        warehouse.addPackage(pkg2)

        val deps = buildDispatchDependencies(
            mutableListOf(vehicle),
            mutableListOf(warehouse, destination)
        )
        val command = DispatchVehicleCommand(
            deps,
            DispatchVehicleRequest("V-1", "WH-1")
        )

        command.execute()
        assertEquals(0, warehouse.cargoQueue.size)
        assertEquals(2, vehicle.assignedPackages.size)

        command.undo()

        assertEquals(2, warehouse.cargoQueue.size)
        assertEquals(0, vehicle.assignedPackages.size)
    }

    @Test
    fun `DispatchVehicleCommand respects vehicle max capacity`() {
        val warehouse = buildWarehouse("WH-1")
        val destination = buildWarehouse("WH-2")
        val vehicle = buildVehicle("V-1", 15.0, warehouse) // only 15 kg capacity

        val pkg1 = buildPackage("PKG-001", 10.0, warehouse, destination)
        val pkg2 = buildPackage("PKG-002", 10.0, warehouse, destination) // won't fit
        warehouse.addPackage(pkg1)
        warehouse.addPackage(pkg2)

        val deps = buildDispatchDependencies(
            mutableListOf(vehicle),
            mutableListOf(warehouse, destination)
        )
        val command = DispatchVehicleCommand(
            deps,
            DispatchVehicleRequest("V-1", "WH-1")
        )

        command.execute()

        // Only pkg1 fits within 15kg capacity
        assertEquals(1, vehicle.assignedPackages.size)
        assertEquals("PKG-001", vehicle.assignedPackages.first().id)
        assertEquals(1, warehouse.cargoQueue.size) // pkg2 stays in warehouse
    }

    // ═══════════════════════════════════════════════════════
    // CommandInvoker Tests
    // ═══════════════════════════════════════════════════════

    @Test
    fun `CommandInvoker execute runs the command and records it in history`() {
        val origin = buildWarehouse("WH-1")
        val destination = buildWarehouse("WH-2")
        val pkg = buildPackage("PKG-001", 10.0, origin, destination)

        val deps = buildAssignDependencies(mutableListOf(origin, destination))
        val command = AssignPackageToCargoQueueCommand(deps, pkg)

        val invoker = CommandInvoker()
        invoker.execute(command)

        assertEquals(1, origin.cargoQueue.size)
    }

    @Test
    fun `CommandInvoker undo reverses the last executed command`() {
        val origin = buildWarehouse("WH-1")
        val destination = buildWarehouse("WH-2")
        val pkg = buildPackage("PKG-001", 10.0, origin, destination)

        val deps = buildAssignDependencies(mutableListOf(origin, destination))
        val command = AssignPackageToCargoQueueCommand(deps, pkg)

        val invoker = CommandInvoker()
        invoker.execute(command)
        assertEquals(1, origin.cargoQueue.size)

        invoker.undo()

        assertEquals(0, origin.cargoQueue.size)
    }

    @Test
    fun `CommandInvoker undo on empty history does nothing`() {
        val invoker = CommandInvoker()
        // Should not throw
        invoker.undo()
    }

    @Test
    fun `CommandInvoker does not record command in history when execute returns false`() {
        val warehouse = buildWarehouse("WH-1")
        val destination = buildWarehouse("WH-2")
        val vehicle = buildVehicle("V-1", 50.0, warehouse)
        // Empty warehouse — dispatch will return false

        val deps = buildDispatchDependencies(
            mutableListOf(vehicle),
            mutableListOf(warehouse, destination)
        )
        val command = DispatchVehicleCommand(
            deps,
            DispatchVehicleRequest("V-1", "WH-1")
        )

        val invoker = CommandInvoker()
        invoker.execute(command)

        // Undo should be a no-op because nothing was recorded
        invoker.undo() // must not throw or cause side effects
        assertEquals(0, warehouse.cargoQueue.size)
        assertEquals(0, vehicle.assignedPackages.size)
    }

    @Test
    fun `CommandInvoker undo only reverses last command leaving earlier commands intact`() {
        val origin = buildWarehouse("WH-1")
        val destination = buildWarehouse("WH-2")

        val pkg1 = buildPackage("PKG-001", 20.0, origin, destination)
        val pkg2 = buildPackage("PKG-002", 10.0, origin, destination)

        val deps = buildAssignDependencies(mutableListOf(origin, destination))

        val invoker = CommandInvoker()
        invoker.execute(AssignPackageToCargoQueueCommand(deps, pkg1))
        invoker.execute(AssignPackageToCargoQueueCommand(deps, pkg2))
        assertEquals(2, origin.cargoQueue.size)

        invoker.undo() // reverses pkg2 assignment only

        assertEquals(1, origin.cargoQueue.size)
        assertEquals("PKG-001", origin.cargoQueue.first().id)
    }

    @Test
    fun `CommandInvoker multiple undos reverse commands in LIFO order`() {
        val origin = buildWarehouse("WH-1")
        val destination = buildWarehouse("WH-2")

        val pkg1 = buildPackage("PKG-001", 20.0, origin, destination)
        val pkg2 = buildPackage("PKG-002", 10.0, origin, destination)
        val pkg3 = buildPackage("PKG-003", 5.0, origin, destination)

        val deps = buildAssignDependencies(mutableListOf(origin, destination))
        val invoker = CommandInvoker()

        invoker.execute(AssignPackageToCargoQueueCommand(deps, pkg1))
        invoker.execute(AssignPackageToCargoQueueCommand(deps, pkg2))
        invoker.execute(AssignPackageToCargoQueueCommand(deps, pkg3))
        assertEquals(3, origin.cargoQueue.size)

        invoker.undo() // removes pkg3 snapshot → 2 packages
        assertEquals(2, origin.cargoQueue.size)

        invoker.undo() // removes pkg2 snapshot → 1 package
        assertEquals(1, origin.cargoQueue.size)

        invoker.undo() // removes pkg1 snapshot → 0 packages
        assertEquals(0, origin.cargoQueue.size)
    }

    @Test
    fun `Full integration dispatch then undo restores initial state`() {
        val warehouse = buildWarehouse("WH-1")
        val destination = buildWarehouse("WH-2")
        val vehicle = buildVehicle("V-1", 100.0, warehouse)

        val pkg1 = buildPackage("PKG-001", 15.0, warehouse, destination)
        val pkg2 = buildPackage("PKG-002", 25.0, warehouse, destination)
        warehouse.addPackage(pkg1)
        warehouse.addPackage(pkg2)

        val deps = buildDispatchDependencies(
            mutableListOf(vehicle),
            mutableListOf(warehouse, destination)
        )

        val invoker = CommandInvoker()
        invoker.execute(
            DispatchVehicleCommand(deps, DispatchVehicleRequest("V-1", "WH-1"))
        )

        // After dispatch
        assertEquals(0, warehouse.cargoQueue.size)
        assertEquals(2, vehicle.assignedPackages.size)

        invoker.undo()

        // After undo — full restoration
        assertEquals(2, warehouse.cargoQueue.size)
        assertEquals(0, vehicle.assignedPackages.size)
    }
}
