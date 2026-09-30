package mainTest.domainTest.usecaseTest.crudTest.logisticspackageTest

import com.google.common.truth.Truth.assertThat
import domain.repository.PackageRepository
import domain.usecase.crud.logisticspackage.DeletePackageUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import domain.exception.EntityValidationException


class DeletePackageUseCaseTest {

    @Test
    fun `returns success when package id is valid and repository delete succeeds`() = runTest {
        // Given
        val packageRepository = mockk<PackageRepository>()
        val useCase = DeletePackageUseCase(packageRepository)

        val packageId = "PKG-000001"

        coEvery {
            packageRepository.delete(packageId)
        } returns Result.success(Unit)

        // When
        val result = useCase(packageId)

        // Then
        assertThat(result.isSuccess).isTrue()

        coVerify(exactly = 1) {
            packageRepository.delete(packageId)
        }
    }

    @Test
    fun `returns failure when repository delete fails`() = runTest {
        // Given
        val packageRepository = mockk<PackageRepository>()
        val useCase = DeletePackageUseCase(packageRepository)

        val packageId = "PKG-000001"
        val exception = RuntimeException("Failed to delete package")

        coEvery {
            packageRepository.delete(packageId)
        } returns Result.failure(exception)

        // When
        val result = useCase(packageId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) {
            packageRepository.delete(packageId)
        }
    }

    @Test
    fun `returns validation failure when package id is invalid`() = runTest {
        // Given
        val packageRepository = mockk<PackageRepository>()
        val useCase = DeletePackageUseCase(packageRepository)

        val invalidId = "ABC-000001"

        // When
        val result = useCase(invalidId)

        // Then
        assertThat(result.isFailure).isTrue()

        assertThat(result.exceptionOrNull())
            .isInstanceOf(EntityValidationException::class.java)

        coVerify(exactly = 0) {
            packageRepository.delete(any())
        }
    }

}
