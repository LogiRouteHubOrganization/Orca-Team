package data.repository.remote

import data.remote.datasource.PackageRemoteDataSource
import data.exception.remote.toDomainException
import data.remote.mapper.toCreateDto
import data.remote.mapper.toDomain
import data.remote.mapper.toUpdateDto
import domain.exception.ResourceNotFoundException
import domain.model.Package
import domain.model.updateinput.PackageUpdateModel
import domain.repository.PackageRepository
import domain.repository.WarehouseRepository

class RemotePackageRepository(
    private val packageRemoteDataSource: PackageRemoteDataSource,
    private val warehouseRepository: WarehouseRepository
) : PackageRepository {

    override suspend fun findById(
        packageId: String
    ): Result<Package?> {

        return runCatching {
            val packageDto = packageRemoteDataSource.getPackageById(packageId)
                ?: return@runCatching null

            val originWarehouse = warehouseRepository
                .findById(packageDto.originWarehouseId)
                .getOrThrow()
                ?: throw ResourceNotFoundException.WarehouseNotFound(
                    packageDto.originWarehouseId
                )

            val destinationWarehouse = warehouseRepository
                .findById(packageDto.destinationWarehouseId)
                .getOrThrow()
                ?: throw ResourceNotFoundException.WarehouseNotFound(
                    packageDto.destinationWarehouseId
                )

            packageDto.toDomain(
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Package",
                resourceId = packageId
            )
        }
    }

    override suspend fun findAll(): Result<List<Package>> {

        return runCatching {
            val packageDtos = packageRemoteDataSource.getAllPackages()

            val warehouses = warehouseRepository
                .findAll()
                .getOrThrow()

            val warehousesById = warehouses.associateBy { it.id }

            packageDtos.map { packageDto ->

                val originWarehouse = warehousesById[
                    packageDto.originWarehouseId
                ] ?: throw ResourceNotFoundException.WarehouseNotFound(
                    packageDto.originWarehouseId
                )

                val destinationWarehouse = warehousesById[
                    packageDto.destinationWarehouseId
                ] ?: throw ResourceNotFoundException.WarehouseNotFound(
                    packageDto.destinationWarehouseId
                )

                packageDto.toDomain(
                    originWarehouse = originWarehouse,
                    destinationWarehouse = destinationWarehouse
                )
            }
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Package"
            )
        }
    }

    override suspend fun add(
        logisticsPackage: Package
    ): Result<Package> {

        return runCatching {
            val requestDto = logisticsPackage.toCreateDto()

            val createdDto = packageRemoteDataSource.createPackage(
                requestDto
            )

            val originWarehouse = warehouseRepository
                .findById(createdDto.originWarehouseId)
                .getOrThrow()
                ?: throw ResourceNotFoundException.WarehouseNotFound(
                    createdDto.originWarehouseId
                )

            val destinationWarehouse = warehouseRepository
                .findById(createdDto.destinationWarehouseId)
                .getOrThrow()
                ?: throw ResourceNotFoundException.WarehouseNotFound(
                    createdDto.destinationWarehouseId
                )

            createdDto.toDomain(
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Package",
                resourceId = logisticsPackage.id
            )
        }
    }

    override suspend fun update(
        packageId: String,
        updateModel: PackageUpdateModel
    ): Result<Package> {

        return runCatching {
            val requestDto = updateModel.toUpdateDto()

            val updatedDto = packageRemoteDataSource.updatePackage(
                id = packageId,
                packageRequest = requestDto
            )

            val originWarehouse = warehouseRepository
                .findById(updatedDto.originWarehouseId)
                .getOrThrow()
                ?: throw ResourceNotFoundException.WarehouseNotFound(
                    updatedDto.originWarehouseId
                )

            val destinationWarehouse = warehouseRepository
                .findById(updatedDto.destinationWarehouseId)
                .getOrThrow()
                ?: throw ResourceNotFoundException.WarehouseNotFound(
                    updatedDto.destinationWarehouseId
                )

            updatedDto.toDomain(
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Package",
                resourceId = packageId
            )
        }
    }

    override suspend fun delete(
        packageId: String
    ): Result<Unit> {

        return runCatching {
            packageRemoteDataSource.deletePackage(packageId)
        }.recoverCatching { exception ->
            throw exception.toDomainException(
                resourceType = "Package",
                resourceId = packageId
            )
        }
    }
}
