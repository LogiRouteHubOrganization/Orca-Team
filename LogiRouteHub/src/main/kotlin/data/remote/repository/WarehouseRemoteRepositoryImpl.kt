package data.remote.repository

import data.remote.datasource.WarehouseRemoteDataSource
import data.remote.mapper.toDomain
import data.remote.mapper.toRequestDto
import data.remote.utils.toDomainException
import domain.exception.DatabaseConflictException
import domain.exception.NetworkUnavailableException
import domain.exception.ResourceNotFoundException
import domain.repository.WarehouseRepository
import domain.model.Warehouse

class WarehouseRemoteRepositoryImpl(
    private val remoteDataSource: WarehouseRemoteDataSource
) : WarehouseRepository {

    override suspend fun findAll(): List<Warehouse> {
        return try {
            remoteDataSource.getAllWarehouses()
                .map { it.toDomain() }
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }

    override suspend fun findById(
        warehouseId: String
    ): Warehouse? {
        return try {
            remoteDataSource.getWarehouseById(warehouseId)
                ?.toDomain()
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }

    override suspend fun add(
        warehouse: Warehouse
    ) {
        try {
            remoteDataSource.createWarehouse(
                warehouse.toRequestDto()
            )
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }

    override suspend fun update(
        warehouse: Warehouse
    ) {
        try {
            remoteDataSource.updateWarehouse(
                warehouse.id,
                warehouse.toRequestDto()
            )
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }

    override suspend fun delete(
        warehouseId: String
    ) {
        try {
            remoteDataSource.deleteWarehouse(warehouseId)
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }
}