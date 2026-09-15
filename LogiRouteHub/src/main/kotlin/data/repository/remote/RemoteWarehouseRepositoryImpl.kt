package data.repository.remote
import data.remote.datasource.WarehouseRemoteDataSource
import data.remote.mapper.toCreateDto
import data.remote.mapper.toDomain
import data.remote.mapper.toUpdateDto
import domain.model.Warehouse
import domain.model.updateinput.WarehouseUpdateModel
import domain.repository.WarehouseRepository

class RemoteWarehouseRepositoryImpl (
    private val warehouseRemoteDataSource: WarehouseRemoteDataSource
    ) : WarehouseRepository {

        override suspend fun findById(
            warehouseId: String
        ): Warehouse? {
            return warehouseRemoteDataSource
                .getWarehouseById(warehouseId)
                ?.toDomain()
        }

        override suspend fun findAll(): List<Warehouse> {
            return warehouseRemoteDataSource
                .getAllWarehouses()
                .map { it.toDomain() }
        }

    override suspend fun add(warehouse: Warehouse): Warehouse {
        val createdDto =
            warehouseRemoteDataSource.createWarehouse(
                warehouse.toCreateDto()
            )

        return createdDto.toDomain()
    }

        override suspend fun update(
            warehouseId: String,
            updateModel: WarehouseUpdateModel
        ): Warehouse {

            val updatedDto =
                warehouseRemoteDataSource.updateWarehouse(
                    id = warehouseId,
                    warehouseRequest = updateModel.toUpdateDto()
                )

            return updatedDto.toDomain()
        }

        override suspend fun delete(warehouseId: String) {
            warehouseRemoteDataSource.deleteWarehouse(warehouseId)
        }
    }
