package domain.command

import domain.command.telemetry.CommandTelemetry
import domain.model.Package
import domain.model.request.ReroutePackageRequest
import domain.repository.PackageRepository
import domain.repository.WarehouseRepository
import domain.usecase.ReroutePackageUseCase

data class ReroutePackageCommandDependencies(
    val reroutePackageUseCase: ReroutePackageUseCase,
    val packageRepository: PackageRepository,
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
        val warehouse =
            dependencies.warehouseRepository
                .findById(input.warehouseId)
                ?: return false

        val indexedPackage = warehouse.cargoQueue
            .withIndex()
            .find {
                it.value.id == input.packageId
            } ?: return false

        previousPackageState = indexedPackage.value
        previousIndex = indexedPackage.index

        val reroutedPackage = dependencies.reroutePackageUseCase(input)
            ?: return false

        afterPackageState = reroutedPackage

        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState = createPackageState(previousPackageState, previousIndex),
            afterState = createPackageState(
                afterPackageState,
                previousIndex
            )
        )

        return true
    }

    override fun undo() {
        val oldPackage = previousPackageState ?: return
        val newPackage = afterPackageState ?: return
        val index = previousIndex ?: return

        dependencies.packageRepository.update(oldPackage)

        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState = createPackageState(newPackage, index),
            afterState = createPackageState(oldPackage, index)
        )
    }

    override fun redo() {
        val oldPackage = previousPackageState ?: return
        val newPackage = afterPackageState ?: return
        val index = previousIndex ?: return

        dependencies.packageRepository.update(newPackage)

        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState = createPackageState(oldPackage, index),
            afterState = createPackageState(
                newPackage,
                index
            )
        )
    }

    private fun createPackageState(
        logisticsPackage: Package?,
        index: Int?
    ): String {
        return "Package: ${logisticsPackage?.id}\n" +
                "Destination: ${logisticsPackage?.destinationWarehouse?.id}\n" +
                "Index: $index"
    }
}
