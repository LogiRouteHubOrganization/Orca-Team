package dataTest.repositoryTest

import data.dataholder.LogisticsPackageRaw
import data.datasource.local.LocalPackageDataSource
import data.datasource.remote.RemotePackageDataSource
import data.exception.local.LocalDataException
import data.mapper.PackageMapper
import data.remote.dto.response.PackageDto
import domain.model.Package
import domain.model.Warehouse
import domain.model.updateinput.PackageUpdateModel
import domain.repository.WarehouseRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import com.google.common.truth.Truth.assertThat
import data.repository.PackageRepositoryImpl


class PackageRepositoryImplTest {

    private lateinit var localDataSource: LocalPackageDataSource
    private lateinit var remoteDataSource: RemotePackageDataSource
    private lateinit var warehouseRepository: WarehouseRepository
    private lateinit var packageMapper: PackageMapper
    private lateinit var repository: PackageRepositoryImpl

    private val originWarehouse = Warehouse(
        id = "WH-000001",
        name = "Gaza Warehouse",
        regionalZone = "North",
        latitude = 30.5,
        longitude = 64.4
    )

    private val destinationWarehouse = Warehouse(
        id = "WH-000002",
        name = "Khan Younis Warehouse",
        regionalZone = "South",
        latitude = 31.3,
        longitude = 34.3
    )

    private val rawPackage = LogisticsPackageRaw(
        packageId = "PKG-000001",
        packageWeightByKg = 5.0,
        originHubId = "WH-000001",
        destinationHubId = "WH-000002",
        packagePriority = "STANDARD"
    )

    private val packageDto = PackageDto(
        id = "PKG-000001",
        weightByKg = 5.0,
        priority = "STANDARD",
        originWarehouseId = "WH-000001",
        destinationWarehouseId = "WH-000002"
    )

    private fun createPackage(
        id: String = rawPackage.packageId,
        weight: Double = rawPackage.packageWeightByKg,
        priority: String = rawPackage.packagePriority
    ) = Package(
        id = id,
        weightByKg = weight,
        priority = priority,
        originWarehouse = originWarehouse,
        destinationWarehouse = destinationWarehouse
    )

    @BeforeEach
    fun setUp() {
        localDataSource = mockk()
        remoteDataSource = mockk()
        warehouseRepository = mockk()
        packageMapper = PackageMapper()

        repository = PackageRepositoryImpl(
            packageDataSource = localDataSource,
            packageRemoteDataSource = remoteDataSource,
            packageMapper = packageMapper,
            warehouseRepository = warehouseRepository
        )

        // Given
        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(originWarehouse)

        coEvery {
            warehouseRepository.findById("WH-000002")
        } returns Result.success(destinationWarehouse)

        coEvery {
            warehouseRepository.findAll()
        } returns Result.success(
            listOf(originWarehouse, destinationWarehouse)
        )
    }

    @Test
    fun `findAll should returns mapped packages`() = runTest {
        // Given
        coEvery {
            localDataSource.loadPackages()
        } returns listOf(rawPackage)

        // When
        val result = repository.findAll()

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).hasSize(1)
        assertThat(result.getOrThrow().first().id)
            .isEqualTo(rawPackage.packageId)
        assertThat(result.getOrThrow().first().originWarehouse.id)
            .isEqualTo(originWarehouse.id)
        assertThat(result.getOrThrow().first().destinationWarehouse.id)
            .isEqualTo(destinationWarehouse.id)
    }

    @Test
    fun `findById should returns matching package`() = runTest {
        // Given
        coEvery {
            localDataSource.loadPackages()
        } returns listOf(rawPackage)

        // When
        val result = repository.findById(rawPackage.packageId)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()?.id)
            .isEqualTo(rawPackage.packageId)
    }

    @Test
    fun `findById should returns null when package does not exist`() = runTest {
        // Given
        coEvery {
            localDataSource.loadPackages()
        } returns listOf(rawPackage)

        // When
        val result = repository.findById("PKG-999999")

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isNull()
    }

    @Test
    fun `add creates remote package and saves it locally`() = runTest {
        // Given
        val logisticsPackage = createPackage()

        coEvery {
            remoteDataSource.createPackage(any())
        } returns packageDto

        coEvery {
            localDataSource.addPackage(any())
        } returns Unit

        coEvery {
            localDataSource.loadPackages()
        } returns listOf(rawPackage)

        // When
        val result = repository.add(logisticsPackage)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().id).isEqualTo(packageDto.id)

        coVerify(exactly = 1) {
            remoteDataSource.createPackage(any())
            localDataSource.addPackage(any())
        }
    }

    @Test
    fun `update updates remote package and local data`() = runTest {
        // Given
        val updateModel = PackageUpdateModel(
            weightByKg = 7.0,
            priority = "URGENT"
        )

        val updatedDto = packageDto.copy(
            weightByKg = 7.0,
            priority = "URGENT"
        )

        val updatedRaw = rawPackage.copy(
            packageWeightByKg = 7.0,
            packagePriority = "URGENT"
        )

        coEvery {
            remoteDataSource.updatePackage(
                id = rawPackage.packageId,
                packageRequest = any()
            )
        } returns updatedDto

        coEvery {
            localDataSource.updatePackage(any())
        } returns Unit

        coEvery {
            localDataSource.loadPackages()
        } returns listOf(updatedRaw)

        // When
        val result = repository.update(
            packageId = rawPackage.packageId,
            updateModel = updateModel
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().weightByKg).isEqualTo(7.0)
        assertThat(result.getOrThrow().priority).isEqualTo("URGENT")

        coVerify(exactly = 1) {
            localDataSource.updatePackage(any())
        }
    }

    @Test
    fun `delete removes package from remote and local sources`() = runTest {
        // Given
        coEvery {
            remoteDataSource.deletePackage(rawPackage.packageId)
        } returns Unit

        coEvery {
            localDataSource.deletePackage(rawPackage.packageId)
        } returns Unit

        coEvery {
            localDataSource.loadPackages()
        } returns emptyList()

        // When
        val result = repository.delete(rawPackage.packageId)

        // Then
        assertThat(result.isSuccess).isTrue()

        coVerify(exactly = 1) {
            remoteDataSource.deletePackage(rawPackage.packageId)
        }
        coVerify(exactly = 1) {
            localDataSource.deletePackage(rawPackage.packageId)
        }
    }

    @Test
    fun `refresh replaces local packages with remote packages`() = runTest {
        // Given
        coEvery {
            remoteDataSource.getAllPackages()
        } returns listOf(packageDto)

        coEvery {
            localDataSource.replacePackages(any())
        } returns Unit

        // When
        val result = repository.refresh()

        // Then
        assertThat(result.isSuccess).isTrue()

        coVerify(exactly = 1) {
            localDataSource.replacePackages(
                match { packages ->
                    packages.size == 1 &&
                            packages.first().packageId == packageDto.id
                }
            )
        }
    }

    @Test
    fun `initializeCargoQueues rebuilds warehouse cargo queues`() = runTest {
        // Given
        coEvery {
            localDataSource.loadPackages()
        } returns listOf(rawPackage)

        // When
        val result = repository.initializeCargoQueues()

        // Then
        assertThat(result.isSuccess).isTrue()

        coVerify(exactly = 1) {
            warehouseRepository.findAll()
        }
        coVerify {
            localDataSource.loadPackages()
        }
    }

    @Test
    fun `findAll returns failure when local data source fails`() = runTest {
        // Given
        coEvery {
            localDataSource.loadPackages()
        } throws LocalDataException("Local storage failed")

        // When
        val result = repository.findAll()

        // Then
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `findAll propagates coroutine cancellation`() = runTest {
        // Given
        coEvery {
            localDataSource.loadPackages()
        } throws CancellationException("Test cancellation")

        // When
        val thrownException = try {
            repository.findAll()
            null
        } catch (exception: CancellationException) {
            exception
        }

        // Then
        assertThat(thrownException).isNotNull()
        assertThat(thrownException?.message)
            .isEqualTo("Test cancellation")
    }
}

