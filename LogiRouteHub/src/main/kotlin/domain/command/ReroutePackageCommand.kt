package domain.command

import domain.command.telemetry.CommandTelemetry
import domain.command.telemetry.PackageState
import domain.model.Package
import domain.model.request.ReroutePackageRequest
import domain.model.updateinput.PackageUpdateModel
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

    override suspend fun execute(): Result<Unit> {
        return runCatching {
            val warehouse =
                dependencies.warehouseRepository
                    .findById(input.warehouseId)
                    .getOrThrow()
                    ?: error("Warehouse not found: ${input.warehouseId}")

            val indexedPackage =
                warehouse.cargoQueue
                    .withIndex()
                    .find { it.value.id == input.packageId }
                    ?: error("Package not found: ${input.packageId}")

            previousPackageState =
                indexedPackage.value

            previousIndex =
                indexedPackage.index

            val reroutedPackage =
                dependencies.reroutePackageUseCase(input)
                    .getOrThrow()
                    ?: error("Package rerouting failed")

            afterPackageState =
                reroutedPackage

            recordStateTransition()

            Unit
        }
    }
    override suspend fun undo(): Result<Unit> {
        return runCatching {
            val oldPackage =
                previousPackageState
                    ?: error("Previous package state is unavailable")

            val newPackage =
                afterPackageState
                    ?: error("After package state is unavailable")

            val index =
                previousIndex
                    ?: error("Previous package index is unavailable")

            dependencies.packageRepository.update(
                oldPackage.id,
                PackageUpdateModel(
                    weightByKg = oldPackage.weightByKg,
                    originWarehouseId =
                        oldPackage.originWarehouse?.id,
                    destinationWarehouseId =
                        oldPackage.destinationWarehouse?.id
                )
            ).getOrThrow()

            dependencies.telemetry.recordStateTransition(
                commandType = commandType,
                beforeState = PackageState(
                    packageId = newPackage.id,
                    destinationWarehouseId =
                        newPackage.destinationWarehouse?.id,
                    index = index
                ),
                afterState = PackageState(
                    packageId = oldPackage.id,
                    destinationWarehouseId =
                        oldPackage.destinationWarehouse?.id,
                    index = index
                )
            )

            Unit
        }
    }

    override suspend fun redo(): Result<Unit> {
        return runCatching {
            val oldPackage =
                previousPackageState
                    ?: error("Previous package state is unavailable")

            val newPackage =
                afterPackageState
                    ?: error("After package state is unavailable")

            val index =
                previousIndex
                    ?: error("Previous package index is unavailable")

            dependencies.packageRepository.update(
                newPackage.id,
                PackageUpdateModel(
                    weightByKg = newPackage.weightByKg,
                    originWarehouseId =
                        newPackage.originWarehouse?.id,
                    destinationWarehouseId =
                        newPackage.destinationWarehouse?.id
                )
            ).getOrThrow()

            dependencies.telemetry.recordStateTransition(
                commandType = commandType,
                beforeState = PackageState(
                    packageId = oldPackage.id,
                    destinationWarehouseId =
                        oldPackage.destinationWarehouse?.id,
                    index = index
                ),
                afterState = PackageState(
                    packageId = newPackage.id,
                    destinationWarehouseId =
                        newPackage.destinationWarehouse?.id,
                    index = index
                )
            )

            Unit
        }
    }

    private fun recordStateTransition() {
        val oldPackage =
            previousPackageState
                ?: return

        val newPackage =
            afterPackageState
                ?: return

        val index =
            previousIndex
                ?: return

        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState = PackageState(
                packageId = oldPackage.id,
                destinationWarehouseId =
                    oldPackage.destinationWarehouse?.id,
                index = index
            ),
            afterState = PackageState(
                packageId = newPackage.id,
                destinationWarehouseId =
                    newPackage.destinationWarehouse?.id,
                index = index
            )
        )
    }
}
