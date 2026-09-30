package mainTest.domainTest.usecaseTest.crudTest.logisticspackageTest

import com.google.common.truth.Truth.assertThat
import domain.model.Package
import domain.model.Warehouse
import domain.repository.PackageRepository
import domain.usecase.crud.logisticspackage.CreatePackageUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CreatePackageUseCaseTest {
    @Test
    fun `returns created package when repository add succeeds`() = runTest {
        // Given
        val packageRepository = mockk<PackageRepository>()

        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        val logisticsPackage = Package(
            id = "PKG-000001",
            weightByKg = 10.0,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        coEvery {
            packageRepository.add(logisticsPackage)
        } returns Result.success(logisticsPackage)

        val useCase = CreatePackageUseCase(packageRepository)

        // When
        val result = useCase(logisticsPackage)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(logisticsPackage)

        coVerify(exactly = 1) {
            packageRepository.add(logisticsPackage)
        }
    }

    @Test
    fun `returns failure when repository add fails`() = runTest {
        // Given
        val packageRepository = mockk<PackageRepository>()

        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        val logisticsPackage = Package(
            id = "PKG-000001",
            weightByKg = 10.0,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        val exception = RuntimeException("Failed to create package")

        coEvery {
            packageRepository.add(logisticsPackage)
        } returns Result.failure(exception)

        val useCase = CreatePackageUseCase(packageRepository)

        // When
        val result = useCase(logisticsPackage)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) {
            packageRepository.add(logisticsPackage)
        }
    }


    private fun createWarehouse(
        id: String
    ): Warehouse {
        return Warehouse(
            id = id,
            name = "Test Warehouse",
            regionalZone = "NORTH",
            latitude = 31.5,
            longitude = 34.5
        )
    }

}
