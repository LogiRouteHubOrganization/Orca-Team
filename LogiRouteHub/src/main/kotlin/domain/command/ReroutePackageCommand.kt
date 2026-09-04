package domain.command

import domain.command.telemetry.CommandTelemetry
import domain.model.Package
import domain.model.request.ReroutePackageRequest
import domain.repository.WarehouseRepository
import domain.usecase.ReroutePackageUseCase

data class ReroutePackageCommandDependencies(
    val reroutePackageUseCase: ReroutePackageUseCase,
    val warehouseRepository: WarehouseRepository,
    val telemetry: CommandTelemetry
)

class ReroutePackageCommand(
    private val dependencies: ReroutePackageCommandDependencies,
    private val input: ReroutePackageRequest
) : Command {

    private var previousPackageState: Package? = null
    private var previousIndex: Int? = null
    private var afterPackageState: Package? = null

    override val commandType = "ReroutePackageCommand"

    override fun execute(): Boolean {
        val warehouse = dependencies.warehouseRepository.findById(input.warehouseId)
            ?: return false

        val indexedPackage = warehouse.cargoQueue
            .withIndex()
            .find { it.value.id == input.packageId }
            ?: return false

        previousPackageState = indexedPackage.value
        previousIndex = indexedPackage.index

        val reroutedPackage = dependencies.reroutePackageUseCase(input)
            ?: return false

        afterPackageState = reroutedPackage

        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState =
                "Package: ${previousPackageState?.id}, " +
                        "Destination: ${previousPackageState?.destinationWarehouse?.id}, " +
                        "Index: $previousIndex",

            afterState =
                "Package: ${afterPackageState?.id}, " +
                        "Destination: ${afterPackageState?.destinationWarehouse?.id}, " +
                        "Index: $previousIndex"
        )

        return true
    }

    override fun undo() {
        val warehouse =
            dependencies.warehouseRepository.findById(input.warehouseId)
                ?: return

        val oldPackage = previousPackageState ?: return
        val newPackage = afterPackageState ?: return
        val oldIndex = previousIndex ?: return

        warehouse.updatePackageAtIndex(
            packageIndex = oldIndex,
            logisticsPackage = oldPackage
        )

        dependencies.warehouseRepository.update(warehouse)

        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState =
                "Package: ${newPackage.id}, " +
                        "Destination: ${newPackage.destinationWarehouse.id}, " +
                        "Index: $oldIndex",
            afterState =
                "Package: ${oldPackage.id}, " +
                        "Destination: ${oldPackage.destinationWarehouse.id}, " +
                        "Index: $oldIndex"
        )
    }

    override fun redo() {
        val warehouse =
            dependencies.warehouseRepository.findById(input.warehouseId)
                ?: return

        val oldPackage = previousPackageState ?: return
        val newPackage = afterPackageState ?: return
        val index = previousIndex ?: return

        warehouse.updatePackageAtIndex(
            packageIndex = index,
            logisticsPackage = newPackage
        )

        dependencies.warehouseRepository.update(warehouse)

        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState =
                "Package: ${oldPackage.id}, " +
                        "Destination: ${oldPackage.destinationWarehouse.id}, " +
                        "Index: $index",
            afterState =
                "Package: ${newPackage.id}, " +
                        "Destination: ${newPackage.destinationWarehouse.id}, " +
                        "Index: $index"
        )
    }
}
