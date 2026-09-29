package main.domain.usecase.crud.logisticspackage

import com.google.common.truth.Truth.assertThat
import domain.exception.EntityValidationException
import domain.model.Package
import domain.model.Warehouse
import domain.repository.PackageRepository
import domain.usecase.crud.logisticspackage.GetPackageByIdUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetPackageByIdUseCaseTest {

    @Test
    fun `returns package when id is valid and package exists`() = runTest {
        // Given
        val packageRepository = mockk<PackageRepository>()
        val useCase = GetPackageByIdUseCase(packageRepository)

        val logisticsPackage = Package(
            id = "PKG-000001",
            weightByKg = 10.0,
            priority = "URGENT",
            originWarehouse = createWarehouse("WH-000001"),
            destinationWarehouse = createWarehouse("WH-000002")
        )

        coEvery {
            packageRepository.findById("PKG-000001")
        } returns Result.success(logisticsPackage)

        // When
        val result = useCase("PKG-000001")

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(logisticsPackage)

        coVerify(exactly = 1) {
            packageRepository.findById("PKG-000001")
        }
    }

        @Test
        fun `returns null when id is valid but package does not exist`() = runTest {
            // Given
            val packageRepository = mockk<PackageRepository>()
            val useCase = GetPackageByIdUseCase(packageRepository)

            coEvery {
                packageRepository.findById("PKG-000001")
            } returns Result.success(null)

            // When
            val result = useCase("PKG-000001")

            // Then
            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrNull()).isNull()

            coVerify(exactly = 1) {
                packageRepository.findById("PKG-000001")
            }
        }

        @Test
        fun `returns failure when repository find by id fails`() = runTest {
            // Given
            val packageRepository = mockk<PackageRepository>()
            val useCase = GetPackageByIdUseCase(packageRepository)

            val exception = RuntimeException("Failed to get package")

            coEvery {
                packageRepository.findById("PKG-000001")
            } returns Result.failure(exception)

            // When
            val result = useCase("PKG-000001")

            // Then
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isEqualTo(exception)

            coVerify(exactly = 1) {
                packageRepository.findById("PKG-000001")
            }
        }

        @Test
        fun `returns validation failure when package id is invalid`() = runTest {
            // Given
            val packageRepository = mockk<PackageRepository>()
            val useCase = GetPackageByIdUseCase(packageRepository)

            val invalidId = "ABC-000001"

            // When
            val result = useCase(invalidId)

            // Then
            assertThat(result.isFailure).isTrue()

            assertThat(result.exceptionOrNull())
                .isInstanceOf(EntityValidationException::class.java)

            coVerify(exactly = 0) {
                packageRepository.findById(any())
            }
        }

        private fun createWarehouse(
            id: String,
            name: String = "Test Warehouse"
        ): Warehouse {
            return Warehouse(
                id = id,
                name = name,
                regionalZone = "NORTH",
                latitude = 31.5,
                longitude = 34.5
            )
        }
}
