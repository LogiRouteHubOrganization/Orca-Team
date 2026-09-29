package main.domain.usecase.crud.logisticspackage

import com.google.common.truth.Truth.assertThat
import domain.model.Package
import domain.model.Warehouse
import domain.model.updateinput.PackageUpdateModel
import domain.repository.PackageRepository
import domain.usecase.crud.logisticspackage.UpdatePackageUseCase
import domain.validator.logisticspackage.UpdatePackageValidator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import domain.exception.EntityValidationException

class UpdatePackageUseCaseTest {

    @Test
    fun `returns updated package when id and update data are valid`() = runTest {
        // Given
        val packageRepository = mockk<PackageRepository>()
        val packageValidator = UpdatePackageValidator()

        val useCase = UpdatePackageUseCase(
            packageRepository = packageRepository,
            packageValidator = packageValidator
        )

        val updateModel = PackageUpdateModel(
            weightByKg = 20.0
        )

        val updatedPackage = Package(
            id = "PKG-000001",
            weightByKg = 20.0,
            priority = "URGENT",
            originWarehouse = createWarehouse("WH-000001"),
            destinationWarehouse = createWarehouse("WH-000002")
        )

        coEvery {
            packageRepository.update(
                "PKG-000001",
                updateModel
            )
        } returns Result.success(updatedPackage)

        // When
        val result = useCase(
            id = "PKG-000001",
            updateRequest = updateModel
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(updatedPackage)

        coVerify(exactly = 1) {
            packageRepository.update(
                "PKG-000001",
                updateModel
            )
        }
    }

    @Test
    fun `returns failure when repository update fails`() = runTest {
        // Given
        val packageRepository = mockk<PackageRepository>()
        val packageValidator = UpdatePackageValidator()

        val useCase = UpdatePackageUseCase(
            packageRepository = packageRepository,
            packageValidator = packageValidator
        )

        val updateModel = PackageUpdateModel(
            weightByKg = 20.0
        )

        val exception = RuntimeException("Failed to update package")

        coEvery {
            packageRepository.update(
                "PKG-000001",
                updateModel
            )
        } returns Result.failure(exception)

        // When
        val result = useCase(
            id = "PKG-000001",
            updateRequest = updateModel
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) {
            packageRepository.update(
                "PKG-000001",
                updateModel
            )
        }
    }

    @Test
    fun `returns validation failure when package id is invalid`() = runTest {
        // Given
        val packageRepository = mockk<PackageRepository>()
        val packageValidator = UpdatePackageValidator()

        val useCase = UpdatePackageUseCase(
            packageRepository = packageRepository,
            packageValidator = packageValidator
        )

        val updateModel = PackageUpdateModel(
            weightByKg = 20.0
        )

        // When
        val result = useCase(
            id = "ABC-000001",
            updateRequest = updateModel
        )

        // Then
        assertThat(result.isFailure).isTrue()

        assertThat(result.exceptionOrNull())
            .isInstanceOf(EntityValidationException::class.java)

        coVerify(exactly = 0) {
            packageRepository.update(any(), any())
        }
    }

    @Test
    fun `returns validation failure when update data is invalid`() = runTest {
        // Given
        val packageRepository = mockk<PackageRepository>()
        val packageValidator = UpdatePackageValidator()

        val useCase = UpdatePackageUseCase(
            packageRepository = packageRepository,
            packageValidator = packageValidator
        )

        val updateModel = PackageUpdateModel(
            weightByKg = 0.0
        )

        // When
        val result = useCase(
            id = "PKG-000001",
            updateRequest = updateModel
        )

        // Then
        assertThat(result.isFailure).isTrue()

        assertThat(result.exceptionOrNull())
            .isInstanceOf(EntityValidationException::class.java)

        coVerify(exactly = 0) {
            packageRepository.update(any(), any())
        }
    }

    @Test
    fun `returns validation failure when update request is empty`() = runTest {
        // Given
        val packageRepository = mockk<PackageRepository>()
        val packageValidator = UpdatePackageValidator()

        val useCase = UpdatePackageUseCase(
            packageRepository = packageRepository,
            packageValidator = packageValidator
        )

        val updateModel = PackageUpdateModel()

        // When
        val result = useCase(
            id = "PKG-000001",
            updateRequest = updateModel
        )

        // Then
        assertThat(result.isFailure).isTrue()

        assertThat(result.exceptionOrNull())
            .isInstanceOf(EntityValidationException::class.java)

        coVerify(exactly = 0) {
            packageRepository.update(any(), any())
        }
    }


    private fun createWarehouse(id: String): Warehouse {
        return Warehouse(
            id = id,
            name = "Test Warehouse",
            regionalZone = "NORTH",
            latitude = 31.5,
            longitude = 34.5
        )
    }
}
