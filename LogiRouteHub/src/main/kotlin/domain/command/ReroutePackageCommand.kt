package domain.command

import domain.model.Package
import domain.model.request.ReroutePackageRequest
import domain.repository.WarehouseRepository
import domain.usecase.ReroutePackageUseCase
import domain.command.telemetry.CommandTelemetry

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
            beforeState = previousPackageState.toString(),
            afterState = afterPackageState.toString()
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
            beforeState = newPackage.toString(),
            afterState = oldPackage.toString()
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
            beforeState = oldPackage.toString(),
            afterState = newPackage.toString()
        )
    }
}
