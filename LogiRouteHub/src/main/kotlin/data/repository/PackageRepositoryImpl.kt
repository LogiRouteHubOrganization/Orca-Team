package data.repository

import data.datasource.local.LocalPackageDataSource
import data.datasource.remote.RemotePackageDataSource
import data.extension.mapLocalDataException
import data.extension.mapRepositoryException
import data.extension.runCatchingCancellable
import data.mapper.PackageMapper
import data.remote.mapper.toCreateDto
import data.remote.mapper.toRaw
import data.remote.mapper.toUpdateDto
import domain.exception.ResourceNotFoundException
import domain.model.Package
import domain.model.updateinput.PackageUpdateModel
import domain.repository.PackageRepository
import domain.repository.WarehouseRepository

class PackageRepositoryImpl(
    private val packageDataSource: LocalPackageDataSource,
    private val packageRemoteDataSource: RemotePackageDataSource,
    private val packageMapper: PackageMapper,
    private val warehouseRepository: WarehouseRepository
) : PackageRepository {

    override suspend fun findAll(): Result<List<Package>> =
        runCatchingCancellable {
            loadLocalPackages()
        }.mapLocalDataException()

    override suspend fun findById(
        packageId: String
    ): Result<Package?> =
        runCatchingCancellable {
            loadLocalPackages().find { it.id == packageId }
        }.mapLocalDataException()

    override suspend fun add(
        logisticsPackage: Package
    ): Result<Package> = runCatchingCancellable {
        val createdDto = packageRemoteDataSource.createPackage(
            logisticsPackage.toCreateDto()
        )
        packageDataSource.addPackage(
            createdDto.toRaw()
        )

        rebuildCargoQueues()
        findLocalPackageOrThrow(
            createdDto.id
        )
    }.mapRepositoryException(
        resourceType = "Package",
        resourceId = logisticsPackage.id
    )

    override suspend fun update(
        packageId: String,
        updateModel: PackageUpdateModel
    ): Result<Package> =
        runCatchingCancellable {
            val updatedDto = packageRemoteDataSource.updatePackage(
                id = packageId,
                packageRequest = updateModel.toUpdateDto()
            )

            packageDataSource.updatePackage(updatedDto.toRaw())
            rebuildCargoQueues()
            findLocalPackageOrThrow(packageId)
        }.mapRepositoryException(
            resourceType = "Package",
            resourceId = packageId
        )

    override suspend fun delete(
        packageId: String
    ): Result<Unit> =
        runCatchingCancellable {
            packageRemoteDataSource.deletePackage(packageId)
            packageDataSource.deletePackage(packageId)
            rebuildCargoQueues()
            Unit
        }.mapRepositoryException(
            resourceType = "Package",
            resourceId = packageId
        )

    override suspend fun refresh(): Result<Unit> =
        runCatchingCancellable {
            val remotePackages = packageRemoteDataSource.getAllPackages()
            val localPackages = remotePackages.map { it.toRaw() }

            packageDataSource.replacePackages(localPackages)
            rebuildCargoQueues()
        }.mapRepositoryException(
            resourceType = "Package"
        )

    private suspend fun loadLocalPackages(): List<Package> {
        val rawPackages = packageDataSource.loadPackages()
        val packages = mutableListOf<Package>()

        for (rawPackage in rawPackages) {
            val originWarehouse = warehouseRepository
                .findById(rawPackage.originHubId)
                .getOrThrow()

            if (originWarehouse == null) {
                println(
                    "Warning: Skipping package '${rawPackage.packageId}' " +
                            "because origin warehouse " +
                            "'${rawPackage.originHubId}' was not found"
                )
                continue
            }

            val destinationWarehouse = warehouseRepository
                .findById(rawPackage.destinationHubId)
                .getOrThrow()

            if (destinationWarehouse == null) {
                println(
                    "Warning: Skipping package '${rawPackage.packageId}' " +
                            "because destination warehouse " +
                            "'${rawPackage.destinationHubId}' was not found"
                )
                continue
            }

            val logisticsPackage = packageMapper.mapToDomainModel(
                rawPackage = rawPackage,
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
            packages.add(logisticsPackage)
        }

        return packages
    }

    private suspend fun findLocalPackageOrThrow(
        packageId: String
    ): Package {
        return loadLocalPackages().find {
            it.id == packageId
        } ?: throw ResourceNotFoundException.PackageNotFound(
            packageId
        )
    }

    override suspend fun initializeCargoQueues(): Result<Unit> =
        runCatchingCancellable {
            rebuildCargoQueues()
        }.mapLocalDataException()

    private suspend fun rebuildCargoQueues() {
        val warehouses = warehouseRepository.findAll().getOrThrow()
        val packages = loadLocalPackages()

        warehouses.forEach { warehouse ->
            val warehousePackages =
                packages.filter { logisticsPackage ->
                    logisticsPackage.originWarehouse.id == warehouse.id
                }
            warehouse.restoreCargoQueue(warehousePackages)
        }
    }
}
