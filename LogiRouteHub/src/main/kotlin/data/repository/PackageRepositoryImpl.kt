package data.repository

import data.datasource.local.LocalPackageDataSource
import data.exception.local.LocalDataException
import data.exception.local.toDomainException
import data.exception.remote.toDomainException
import data.mapper.PackageMapper
import data.datasource.remote.RemotePackageDataSource
import data.remote.mapper.toCreateDto
import data.remote.mapper.toRaw
import data.remote.mapper.toUpdateDto
import domain.exception.ResourceNotFoundException
import domain.model.Package
import domain.repository.PackageRepository
import domain.repository.WarehouseRepository
import domain.model.updateinput.PackageUpdateModel

class PackageRepositoryImpl(
    private val packageDataSource: LocalPackageDataSource,
    private val packageRemoteDataSource: RemotePackageDataSource,
    private val packageMapper: PackageMapper,
    private val warehouseRepository: WarehouseRepository
) : PackageRepository {

    override suspend fun findAll(): Result<List<Package>> =
        runCatching {
            loadLocalPackages()
        }.mapLocalDataException()

    override suspend fun findById(
        packageId: String
    ): Result<Package?> =
        runCatching {
            loadLocalPackages().find {
                it.id == packageId
            }
        }.mapLocalDataException()

    override suspend fun add(
        logisticsPackage: Package
    ): Result<Package> = runCatching {
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
            resourceId = logisticsPackage.id
        )

    override suspend fun update(
        packageId: String,
        updateModel: PackageUpdateModel
    ): Result<Package> = runCatching {
        val updatedDto = packageRemoteDataSource.updatePackage(
                    id = packageId,
                    packageRequest = updateModel.toUpdateDto()
                )

            packageDataSource.updatePackage(
                updatedDto.toRaw()
            )

            rebuildCargoQueues()

            findLocalPackageOrThrow(
                packageId
            )
        }.mapRepositoryException(
            resourceId = packageId
        )

    override suspend fun delete(
        packageId: String
    ): Result<Unit> =
        runCatching {
            packageRemoteDataSource.deletePackage(
                packageId
            )
            packageDataSource.deletePackage(
                packageId
            )
            rebuildCargoQueues()
            Unit
        }.mapRepositoryException(
            resourceId = packageId
        )

    override suspend fun refresh(): Result<Unit> =
        runCatching {
            val remotePackages =
                packageRemoteDataSource.getAllPackages()

            val localPackages =
                remotePackages.map { packageDto ->
                    packageDto.toRaw()
                }

            packageDataSource.replacePackages(
                localPackages
            )
        }.mapRepositoryException()

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
                    .findById(rawPackage.destinationHubId).getOrThrow()

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

    private fun <T> Result<T>.mapLocalDataException(): Result<T> {
        return recoverCatching { throwable ->
            throw when (throwable) {
                is LocalDataException ->
                    throwable.toDomainException()

                else -> throwable
            }
        }
    }

    private fun <T> Result<T>.mapRepositoryException(
        resourceId: String? = null
    ): Result<T> {
        return recoverCatching { throwable ->
            throw when (throwable) {
                is LocalDataException ->
                    throwable.toDomainException()

                else -> {
                    if (resourceId != null) {
                        throwable.toDomainException(
                            resourceType = "Package",
                            resourceId = resourceId
                        )
                    } else {
                        throwable.toDomainException(
                            resourceType = "Package"
                        )
                    }
                }
            }
        }
    }
    override suspend fun initializeCargoQueues(): Result<Unit> =
        runCatching {
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

            warehouse.restoreCargoQueue(
                warehousePackages
            )
        }
    }
}
