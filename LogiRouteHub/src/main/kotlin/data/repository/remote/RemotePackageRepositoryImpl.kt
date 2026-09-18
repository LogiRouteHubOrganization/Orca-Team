package data.repository.remote

import data.remote.datasource.PackageRemoteDataSource
import data.remote.exception.toDomainException
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

    override suspend fun findById(packageId: String): Package? {
        return try {
            val packageDto = packageRemoteDataSource.getPackageById(packageId)
                ?: return null

            val originWarehouse = warehouseRepository.findById(packageDto.originWarehouseId)
                ?: throw ResourceNotFoundException("Warehouse", packageDto.originWarehouseId)

            val destinationWarehouse = warehouseRepository.findById(packageDto.destinationWarehouseId)
                ?: throw ResourceNotFoundException("Warehouse", packageDto.destinationWarehouseId)

            packageDto.toDomain(
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Package", resourceId = packageId)
        }
    }

    override suspend fun findAll(): List<Package> {
        return try {
            val packageDto = packageRemoteDataSource.getAllPackages()
            val warehouses = warehouseRepository.findAll()
            val warehousesById = warehouses.associateBy { it.id }

            packageDto.map { packageDto ->
                val originWarehouse = warehousesById[packageDto.originWarehouseId]
                    ?: throw ResourceNotFoundException("Warehouse", packageDto.originWarehouseId)

                val destinationWarehouse = warehousesById[packageDto.destinationWarehouseId]
                    ?: throw ResourceNotFoundException("Warehouse", packageDto.destinationWarehouseId)

                packageDto.toDomain(
                    originWarehouse = originWarehouse,
                    destinationWarehouse = destinationWarehouse
                )
            }
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Package")
        }
    }

    override suspend fun add(logisticsPackage: Package): Package {
        return try {
            val requestDto = logisticsPackage.toCreateDto()
            val createdDto = packageRemoteDataSource.createPackage(requestDto)

            val originWarehouse = warehouseRepository.findById(createdDto.originWarehouseId)
                ?: throw ResourceNotFoundException("Warehouse", createdDto.originWarehouseId)

            val destinationWarehouse = warehouseRepository.findById(createdDto.destinationWarehouseId)
                ?: throw ResourceNotFoundException("Warehouse", createdDto.destinationWarehouseId)

            createdDto.toDomain(
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Package", resourceId = logisticsPackage.id)
        }
    }

    override suspend fun update(
        packageId: String,
        updateModel: PackageUpdateModel
    ): Package {
        return try {
            val requestDto = updateModel.toUpdateDto()
            val updatedDto = packageRemoteDataSource.updatePackage(
                id = packageId,
                packageRequest = requestDto
            )

            val originWarehouse = warehouseRepository.findById(updatedDto.originWarehouseId)
                ?: throw ResourceNotFoundException("Warehouse", updatedDto.originWarehouseId)

            val destinationWarehouse = warehouseRepository.findById(updatedDto.destinationWarehouseId)
                ?: throw ResourceNotFoundException("Warehouse", updatedDto.destinationWarehouseId)

            updatedDto.toDomain(
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Package", resourceId = packageId)
        }
    }

    override suspend fun delete(packageId: String) {
        try {
            packageRemoteDataSource.deletePackage(packageId)
        } catch (e: Exception) {
            throw e.toDomainException(resourceType = "Package", resourceId = packageId)
        }
    }
}
