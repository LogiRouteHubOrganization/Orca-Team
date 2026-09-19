package data.repository.local

import data.datasource.PackageDataSource
import data.mapper.PackageMapper
import domain.exception.ResourceNotFoundException
import domain.model.Package
import domain.model.Warehouse
import domain.model.updateinput.PackageUpdateModel
import domain.repository.PackageRepository
import domain.repository.WarehouseRepository

class LocalPackageRepositoryImpl(
    private val packageDataSource: PackageDataSource,
    private val packageMapper: PackageMapper,
    private val warehouseRepository: WarehouseRepository
) : PackageRepository {

    companion object {
        private const val NOT_FOUND_INDEX = -1
    }

    private val packages = mutableListOf<Package>()
    private var isLoaded = false

    private suspend fun loadPackagesIfNeeded() {
        if (isLoaded) return

        val rawPackages = packageDataSource.loadPackages()

        for (rawPackage in rawPackages) {

            val originWarehouse =
                warehouseRepository
                    .findById(rawPackage.originHubId)
                    .getOrThrow()
                    ?: throw ResourceNotFoundException(
                        "Warehouse",
                        rawPackage.originHubId
                    )

            val destinationWarehouse =
                warehouseRepository
                    .findById(rawPackage.destinationHubId)
                    .getOrThrow()
                    ?: throw ResourceNotFoundException(
                        "Warehouse",
                        rawPackage.destinationHubId
                    )

            val logisticsPackage =
                packageMapper.mapToDomainModel(
                    rawPackage = rawPackage,
                    originWarehouse = originWarehouse,
                    destinationWarehouse = destinationWarehouse
                )

            packages.add(logisticsPackage)
            originWarehouse.addPackage(logisticsPackage)
        }

        isLoaded = true
    }

    override suspend fun findById(
        packageId: String
    ): Result<Package?> =
        runCatching {
            loadPackagesIfNeeded()

            packages.find { it.id == packageId }
        }

    override suspend fun findAll(): Result<List<Package>> =
        runCatching {
            loadPackagesIfNeeded()

            packages.toList()
        }

    override suspend fun add(
        logisticsPackage: Package
    ): Result<Package> =
        runCatching {
            loadPackagesIfNeeded()

            packages.add(logisticsPackage)
            logisticsPackage.originWarehouse
                .addPackage(logisticsPackage)

            logisticsPackage
        }

    override suspend fun update(
        packageId: String,
        updateModel: PackageUpdateModel
    ): Result<Package> =
        runCatching {
            loadPackagesIfNeeded()

            val currentPackage =
                findPackageByIdOrThrow(packageId)

            val updatedWarehouses =
                resolveWarehouses(
                    currentPackage,
                    updateModel
                )

            val updatedPackage =
                createUpdatedPackage(
                    currentPackage,
                    updateModel,
                    updatedWarehouses
                )

            applyPackageUpdate(
                currentPackage,
                updatedPackage
            )

            updatedPackage
        }

    private fun findPackageByIdOrThrow(
        packageId: String
    ): Package {

        return packages.find { it.id == packageId }
            ?: throw ResourceNotFoundException(
                "Package",
                packageId
            )
    }

    private suspend fun resolveWarehouses(
        currentPackage: Package,
        updateModel: PackageUpdateModel
    ): Pair<Warehouse, Warehouse> {

        val originWarehouse =
            findUpdatedWarehouse(
                updateModel.originWarehouseId,
                currentPackage.originWarehouse
            )

        val destinationWarehouse =
            findUpdatedWarehouse(
                updateModel.destinationWarehouseId,
                currentPackage.destinationWarehouse
            )

        return originWarehouse to destinationWarehouse
    }

    private suspend fun findUpdatedWarehouse(
        warehouseId: String?,
        currentWarehouse: Warehouse
    ): Warehouse {

        return warehouseId?.let { id ->

            warehouseRepository
                .findById(id)
                .getOrThrow()
                ?: throw ResourceNotFoundException(
                    "Warehouse",
                    id
                )

        } ?: currentWarehouse
    }

    private fun createUpdatedPackage(
        currentPackage: Package,
        updateModel: PackageUpdateModel,
        warehouses: Pair<Warehouse, Warehouse>
    ): Package {

        return currentPackage.copy(
            weightByKg = updateModel.weightByKg
                ?: currentPackage.weightByKg,

            priority = updateModel.priority
                ?: currentPackage.priority,

            originWarehouse = warehouses.first,
            destinationWarehouse = warehouses.second
        )
    }

    private fun applyPackageUpdate(
        currentPackage: Package,
        updatedPackage: Package
    ) {
        updateWarehouseCargo(
            currentPackage,
            updatedPackage
        )

        replacePackageInRepository(updatedPackage)
    }

    private fun updateWarehouseCargo(
        currentPackage: Package,
        updatedPackage: Package
    ) {

        val originChanged =
            currentPackage.originWarehouse.id !=
                    updatedPackage.originWarehouse.id

        if (originChanged) {

            currentPackage.originWarehouse
                .removePackage(currentPackage)

            updatedPackage.originWarehouse
                .addPackage(updatedPackage)

            return
        }

        val packageIndex =
            updatedPackage.originWarehouse
                .cargoQueue
                .indexOfFirst {
                    it.id == updatedPackage.id
                }

        if (packageIndex != NOT_FOUND_INDEX) {

            updatedPackage.originWarehouse
                .updatePackageAtIndex(
                    packageIndex,
                    updatedPackage
                )
        }
    }

    private fun replacePackageInRepository(
        updatedPackage: Package
    ) {

        val packageIndex =
            packages.indexOfFirst {
                it.id == updatedPackage.id
            }

        packages[packageIndex] = updatedPackage
    }

    override suspend fun delete(
        packageId: String
    ): Result<Unit> =
        runCatching {
            loadPackagesIfNeeded()

            val logisticsPackage =
                packages.find {
                    it.id == packageId
                } ?: throw ResourceNotFoundException(
                    "Package",
                    packageId
                )

            logisticsPackage.originWarehouse
                .removePackage(logisticsPackage)

            packages.remove(logisticsPackage)
        }
}