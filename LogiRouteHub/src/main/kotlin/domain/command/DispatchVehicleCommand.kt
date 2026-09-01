import domain.command.Command
import domain.model.Package
import domain.model.Vehicle
import domain.model.Warehouse
import domain.model.request.DispatchVehicleRequest
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository
import domain.usecase.DispatchVehicleUseCase

data class DispatchVehicleCommandDependencies(
    val dispatchVehicleUseCase: DispatchVehicleUseCase,
    val vehicleRepository: VehicleRepository,
    val warehouseRepository: WarehouseRepository
)

class DispatchVehicleCommand(
    private val dependencies: DispatchVehicleCommandDependencies,
    private val request: DispatchVehicleRequest
) : Command {
    private var vehicle: Vehicle? = null
    private var warehouse: Warehouse? = null

    private var previousVehiclePackages: List<Package>? = null
    private var previousWarehouseQueue: List<Package>? = null

    override fun execute(): Boolean {
        val currentVehicle =
            dependencies.vehicleRepository.findById(request.vehicleId) ?: return false
        val currentWarehouse =
            dependencies.warehouseRepository.findById(request.warehouseId) ?: return false
        vehicle = currentVehicle
        warehouse = currentWarehouse

        previousVehiclePackages = currentVehicle.assignedPackages.toList()
        previousWarehouseQueue = currentWarehouse.cargoQueue.toList()
        val dispatchedPackages = dependencies.dispatchVehicleUseCase(request)
        return dispatchedPackages.isNotEmpty()
    }

    override fun undo() {
        val executedVehicle = vehicle ?: return
        val executedWarehouse = warehouse ?: return

        val oldVehiclePackages = previousVehiclePackages ?: return
        val oldWarehouseQueue = previousWarehouseQueue ?: return

        executedVehicle.restoreAssignedPackages(oldVehiclePackages)
        executedWarehouse.restoreCargoQueue(oldWarehouseQueue)

        dependencies.vehicleRepository.update(executedVehicle)
        dependencies.warehouseRepository.update(executedWarehouse)
    }
}
