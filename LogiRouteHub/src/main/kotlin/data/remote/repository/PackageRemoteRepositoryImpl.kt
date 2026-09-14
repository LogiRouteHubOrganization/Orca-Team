package data.remote.repository

import data.remote.datasource.PackageRemoteDataSource
import data.remote.mapper.toDomain
import data.remote.mapper.toRequestDto
import data.remote.utils.toDomainException
import domain.model.Package
import domain.repository.PackageRepository
import domain.repository.WarehouseRepository

class PackageRemoteRepositoryImpl(
    private val remoteDataSource: PackageRemoteDataSource,
    private val warehouseRepository: WarehouseRepository
) : PackageRepository {

    override suspend fun findAll(): List<Package> {
        return try {
            remoteDataSource.getAllPackages().mapNotNull { dto ->
                val originWarehouse =
                    warehouseRepository.findById(dto.originWarehouseId)

                val destinationWarehouse =
                    warehouseRepository.findById(dto.destinationWarehouseId)

                if (originWarehouse == null || destinationWarehouse == null) {
                    null
                } else {
                    dto.toDomain(
                        originWarehouse = originWarehouse,
                        destinationWarehouse = destinationWarehouse
                    )
                }
            }
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }

    override suspend fun findById(
        packageId: String
    ): Package? {
        return try {
            val dto = remoteDataSource.getPackageById(packageId)
                ?: return null

            val originWarehouse =
                warehouseRepository.findById(dto.originWarehouseId)
                    ?: return null

            val destinationWarehouse =
                warehouseRepository.findById(dto.destinationWarehouseId)
                    ?: return null

            dto.toDomain(
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }

    override suspend fun add(
        logisticsPackage: Package
    ) {
        try {
            remoteDataSource.createPackage(
                logisticsPackage.toRequestDto()
            )
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }

    override suspend fun update(
        logisticsPackage: Package
    ) {
        try {
            remoteDataSource.updatePackage(
                logisticsPackage.id,
                logisticsPackage.toRequestDto()
            )
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }

    override suspend fun delete(
        packageId: String
    ) {
        try {
            remoteDataSource.deletePackage(packageId)
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }
}