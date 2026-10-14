package dataTest.repositoryTest

import com.google.common.truth.Truth.assertThat
import data.dataholder.WarehouseRaw
import data.datasource.local.LocalWarehouseDataSource
import data.datasource.remote.RemoteWarehouseDataSource
import data.mapper.WarehouseMapper
import data.remote.dto.response.WarehouseDto
import data.repository.WarehouseRepositoryImpl
import domain.model.updateinput.WarehouseUpdateModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class WarehouseRepositoryImplTest {

    private lateinit var localDataSource: LocalWarehouseDataSource
    private lateinit var remoteDataSource: RemoteWarehouseDataSource
    private lateinit var warehouseMapper: WarehouseMapper
    private lateinit var repository: WarehouseRepositoryImpl

    private val rawWarehouse = WarehouseRaw(
        warehouseId = "WH-000001",
        warehouseName = "Gaza Warehouse",
        regionalZone = "North",
        latitude = 54.5,
        longitude = 40.4
    )

    private val secondRawWarehouse = WarehouseRaw(
        warehouseId = "WH-000002",
        warehouseName = "Khan Younis Warehouse",
        regionalZone = "South",
        latitude = 20.3,
        longitude = 32.3
    )


    private fun createWarehouseDto(
        id: String = rawWarehouse.warehouseId,
        name: String = rawWarehouse.warehouseName,
        regionalZone: String = rawWarehouse.regionalZone,
        latitude: Double = rawWarehouse.latitude,
        longitude: Double = rawWarehouse.longitude
    ): WarehouseDto {
        val dto = mockk<WarehouseDto>()

        every { dto.id } returns id
        every { dto.name } returns name
        every { dto.regionalZone } returns regionalZone
        every { dto.latitude } returns latitude
        every { dto.longitude } returns longitude

        return dto
    }


    @BeforeEach
    fun setUp() {
        localDataSource = mockk()
        remoteDataSource = mockk()
        warehouseMapper = WarehouseMapper()

        repository = WarehouseRepositoryImpl(
            warehouseDataSource = localDataSource,
            warehouseRemoteDataSource = remoteDataSource,
            warehouseMapper = warehouseMapper
        )
    }

    @Test
    fun `findAll returns all mapped warehouses`() = runTest {
        // Given
        coEvery { localDataSource.loadWarehouses()
        } returns listOf(rawWarehouse, secondRawWarehouse)

        // When
        val result = repository.findAll()

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().map { it.id })
            .containsExactly("WH-000001", "WH-000002")
            .inOrder()

        coVerify(exactly = 1) { localDataSource.loadWarehouses() }
    }

    @Test
    fun `findById returns matching warehouse`() = runTest {
        // Given
        coEvery {
            localDataSource.loadWarehouses()
        } returns listOf(rawWarehouse, secondRawWarehouse)

        // When
        val result = repository.findById("WH-000002")

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()?.name)
            .isEqualTo("Khan Younis Warehouse")
    }

    @Test
    fun `findById returns null when warehouse does not exist`() = runTest {
        // Given
        coEvery {
            localDataSource.loadWarehouses()
        } returns listOf(rawWarehouse)

        // When
        val result = repository.findById("WH-000999")

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isNull()
    }

    @Test
    fun `findAll loads local data only once while cache is loaded`() = runTest {
        // Given
        coEvery {
            localDataSource.loadWarehouses()
        } returns listOf(rawWarehouse)

        // When
        repository.findAll()
        repository.findAll()

        // Then
        coVerify(exactly = 1) { localDataSource.loadWarehouses() }
    }

    @Test
    fun `add creates remote warehouse and updates local data and cache`() =
        runTest {
            // Given
            coEvery {
                localDataSource.loadWarehouses()
            } returns emptyList()

            coEvery {
                remoteDataSource.createWarehouse(any())
            } returns createWarehouseDto()

            coEvery {
                localDataSource.addWarehouse(any())
            } returns Unit

            val warehouse = warehouseMapper.mapToDomainModel(rawWarehouse)

            // When
            val result = repository.add(warehouse)

            // Then
            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrThrow().id)
                .isEqualTo(rawWarehouse.warehouseId)

            coVerify(exactly = 1) {
                remoteDataSource.createWarehouse(any())
                localDataSource.addWarehouse(any())
            }

            val cachedResult = repository.findById(rawWarehouse.warehouseId)

            assertThat(cachedResult.getOrThrow()?.name)
                .isEqualTo(rawWarehouse.warehouseName)
        }

    @Test
    fun `update changes warehouse details in cache`() = runTest {
        // Given
        coEvery {
            localDataSource.loadWarehouses()
        } returns listOf(rawWarehouse)

        coEvery {
            remoteDataSource.updateWarehouse(any(), any())
        } returns createWarehouseDto(
            name = "Updated Warehouse",
            regionalZone = "Central",
            latitude = 31.6,
            longitude = 33.5
        )

        coEvery {
            localDataSource.updateWarehouse(any())
        } returns Unit

        val updateModel = mockk<WarehouseUpdateModel>(relaxed = true)

        // When
        val result = repository.update(
            warehouseId = rawWarehouse.warehouseId,
            updateModel = updateModel
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().name)
            .isEqualTo("Updated Warehouse")
        assertThat(result.getOrThrow().regionalZone)
            .isEqualTo("Central")

        coVerify(exactly = 1) {
            remoteDataSource.updateWarehouse(any(), any())
            localDataSource.updateWarehouse(any())
        }
    }

    @Test
    fun `delete removes warehouse from local data and cache`() = runTest {
        // Given
        coEvery {
            localDataSource.loadWarehouses()
        } returns listOf(rawWarehouse)

        coEvery {
            remoteDataSource.deleteWarehouse(rawWarehouse.warehouseId)
        } returns Unit

        coEvery {
            localDataSource.deleteWarehouse(rawWarehouse.warehouseId)
        } returns Unit

        // When
        val result = repository.delete(rawWarehouse.warehouseId)

        // Then
        assertThat(result.isSuccess).isTrue()

        coVerify(exactly = 1) {
            remoteDataSource.deleteWarehouse(rawWarehouse.warehouseId)
            localDataSource.deleteWarehouse(rawWarehouse.warehouseId)
        }

        val cachedResult = repository.findById(rawWarehouse.warehouseId)

        assertThat(cachedResult.getOrThrow()).isNull()
    }

    @Test
    fun `refresh replaces local data with remote warehouses`() = runTest {
        // Given
        val remoteWarehouse = createWarehouseDto(
            id = secondRawWarehouse.warehouseId,
            name = secondRawWarehouse.warehouseName,
            regionalZone = secondRawWarehouse.regionalZone,
            latitude = secondRawWarehouse.latitude,
            longitude = secondRawWarehouse.longitude
        )

        coEvery {
            remoteDataSource.getAllWarehouses()
        } returns listOf(remoteWarehouse)

        coEvery {
            localDataSource.replaceWarehouses(any())
        } returns Unit

        coEvery {
            localDataSource.loadWarehouses()
        } returns listOf(secondRawWarehouse)

        // When
        val result = repository.refresh()

        // Then
        assertThat(result.isSuccess).isTrue()

        coVerify(exactly = 1) {
            remoteDataSource.getAllWarehouses()
            localDataSource.replaceWarehouses(
                match { warehouses ->
                    warehouses.size == 1 &&
                            warehouses.single().warehouseId ==
                            secondRawWarehouse.warehouseId
                }
            )
        }

        val refreshedResult = repository.findAll()

        assertThat(refreshedResult.getOrThrow().map { it.id })
            .containsExactly(secondRawWarehouse.warehouseId)
    }

    @Test
    fun `findAll returns failure when local data source fails`() = runTest {
        // Given
        coEvery {
            localDataSource.loadWarehouses()
        } throws IllegalStateException("Local storage failed")

        // When
        val result = repository.findAll()

        // Then
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `findAll propagates coroutine cancellation`() = runTest {
        // Given
        coEvery {
            localDataSource.loadWarehouses()
        } throws CancellationException("Test cancellation")

        // When
        val exception = assertFailsWith<CancellationException> {
            repository.findAll()
        }

        // Then
        assertThat(exception.message).isEqualTo("Test cancellation")
    }

}

