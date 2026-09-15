package data.repository.remote

import data.remote.datasource.PackageRemoteDataSource
import data.remote.mapper.toCreateDto
import data.remote.mapper.toDomain
import data.remote.mapper.toUpdateDto
import domain.model.Package
import domain.model.updateinput.PackageUpdateModel
import domain.repository.PackageRepository
import domain.repository.WarehouseRepository

class RemotePackageRepository(
    private val packageRemoteDataSource: PackageRemoteDataSource,
    private val warehouseRepository: WarehouseRepository
) : PackageRepository {

    override suspend fun findById(packageId: String): Package? {
        val packageDto =
            packageRemoteDataSource.getPackageById(packageId)
                ?: return null

        val originWarehouse =
            warehouseRepository.findById(packageDto.originWarehouseId)
                ?: throw IllegalStateException(
                    "Origin warehouse not found: ${packageDto.originWarehouseId}"
                )

        val destinationWarehouse =
            warehouseRepository.findById(packageDto.destinationWarehouseId)
                ?: throw IllegalStateException(
                    "Destination warehouse not found: ${packageDto.destinationWarehouseId}"
                )

        return packageDto.toDomain(
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }

    override suspend fun findAll(): List<Package> {
        val packageDtos = packageRemoteDataSource.getAllPackages()

        val warehouses = warehouseRepository.findAll()
        val warehousesById = warehouses.associateBy { it.id }

        return packageDtos.map { packageDto ->
            val originWarehouse =
                warehousesById[packageDto.originWarehouseId]
                    ?: throw IllegalStateException(
                        "Origin warehouse not found: ${packageDto.originWarehouseId}"
                    )

            val destinationWarehouse =
                warehousesById[packageDto.destinationWarehouseId]
                    ?: throw IllegalStateException(
                        "Destination warehouse not found: ${packageDto.destinationWarehouseId}"
                    )

            packageDto.toDomain(
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }
    }
    override suspend fun add(logisticsPackage: Package): Package {
        val requestDto = logisticsPackage.toCreateDto()

        val createdDto =
            packageRemoteDataSource.createPackage(requestDto)

        val originWarehouse =
            warehouseRepository.findById(createdDto.originWarehouseId)
                ?: throw IllegalStateException(
                    "Origin warehouse not found: ${createdDto.originWarehouseId}"
                )

        val destinationWarehouse =
            warehouseRepository.findById(createdDto.destinationWarehouseId)
                ?: throw IllegalStateException(
                    "Destination warehouse not found: ${createdDto.destinationWarehouseId}"
                )

        return createdDto.toDomain(
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }

    override suspend fun update(
        packageId: String,
        updateModel: PackageUpdateModel
    ): Package {
        val requestDto = updateModel.toUpdateDto()

        val updatedDto =
            packageRemoteDataSource.updatePackage(
                id = packageId,
                packageRequest = requestDto
            )

        val originWarehouse =
            warehouseRepository.findById(updatedDto.originWarehouseId)
                ?: throw IllegalStateException(
                    "Origin warehouse not found: ${updatedDto.originWarehouseId}"
                )

        val destinationWarehouse =
            warehouseRepository.findById(updatedDto.destinationWarehouseId)
                ?: throw IllegalStateException(
                    "Destination warehouse not found: ${updatedDto.destinationWarehouseId}"
                )

        return updatedDto.toDomain(
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }

    override suspend fun delete(packageId: String) {
        packageRemoteDataSource.deletePackage(packageId)
    }
}