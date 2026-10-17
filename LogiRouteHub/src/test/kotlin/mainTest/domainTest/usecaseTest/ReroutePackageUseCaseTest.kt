package mainTest.domainTest.usecaseTest

import kotlinx.coroutines.test.runTest
import com.google.common.truth.Truth.assertThat
import domain.exception.EntityValidationException
import domain.model.Package
import domain.model.Warehouse
import domain.model.request.ReroutePackageRequest
import domain.model.updateinput.PackageUpdateModel
import domain.repository.PackageRepository
import domain.repository.WarehouseRepository
import domain.usecase.ReroutePackageUseCase
import domain.validator.ErrorValidator
import domain.validator.FieldValidator
import domain.validator.ResultValidator
import domain.validator.logisticspackage.UpdatePackageValidator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class ReroutePackageUseCaseTest {

    private val packageRepository = mockk<PackageRepository>()
    private val warehouseRepository = mockk<WarehouseRepository>()
    private val updatePackageValidator = mockk<UpdatePackageValidator>()

    private val useCase = ReroutePackageUseCase(
        packageRepository = packageRepository,
        warehouseRepository = warehouseRepository,
        updatePackageValidator = updatePackageValidator
    )

    private val warehouse = mockk<Warehouse>()
    private val newDestinationWarehouse = mockk<Warehouse>()
    private val logisticsPackage = mockk<Package>()
    private val reroutedPackage = mockk<Package>()

    private val input = ReroutePackageRequest(
        warehouseId = "WH-123456",
        packageId = "PKG-123456",
        newDestinationWarehouse = newDestinationWarehouse
    )

    @Test
    fun `successfully reroutes package`() = runTest {
        // Given
        every { warehouse.cargoQueue } returns listOf(logisticsPackage)
        every { logisticsPackage.id } returns input.packageId
        every { newDestinationWarehouse.id } returns "WH-654321"

        every {
            logisticsPackage.copy(
                destinationWarehouse = newDestinationWarehouse
            )
        } returns reroutedPackage

        every { reroutedPackage.id } returns input.packageId

        val updateModel = PackageUpdateModel(
            destinationWarehouseId = newDestinationWarehouse.id
        )

        coEvery {
            warehouseRepository.findById(input.warehouseId)
        } returns Result.success(warehouse)

        every {
            updatePackageValidator(updateModel)
        } returns ResultValidator.Valid

        coEvery {
            packageRepository.update(
                input.packageId,
                updateModel
            )
        } returns Result.success(reroutedPackage)

        // When
        val result = useCase(input)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(reroutedPackage)

        verify(exactly = 1) {
            updatePackageValidator(updateModel)
        }

        coVerify(exactly = 1) {
            packageRepository.update(
                input.packageId,
                updateModel
            )
        }
    }

    @Test
    fun `returns null when warehouse is not found`()  = runTest{
        // Given
        coEvery {
            warehouseRepository.findById(input.warehouseId)
        } returns Result.success(null)

        // When
        val result = useCase(input)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNull()

        verify(exactly = 0) {
            updatePackageValidator(any())
        }

        coVerify(exactly = 0) {
            packageRepository.update(any(), any())
        }
    }

    @Test
    fun `returns null when package is not found in warehouse cargo queue`() = runTest {
        // Given
        every { warehouse.cargoQueue } returns emptyList()

        coEvery {
            warehouseRepository.findById(input.warehouseId)
        } returns Result.success(warehouse)

        // When
        val result = useCase(input)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNull()

        verify(exactly = 0) {
            updatePackageValidator(any())
        }

        coVerify(exactly = 0) {
            packageRepository.update(any(), any())
        }
    }

    @Test
    fun `returns failure when package update validation fails`() = runTest {
        // Given
        every { warehouse.cargoQueue } returns listOf(logisticsPackage)
        every { logisticsPackage.id } returns input.packageId
        every { newDestinationWarehouse.id } returns "WH-654321"

        every {
            logisticsPackage.copy(
                destinationWarehouse = newDestinationWarehouse)
        } returns reroutedPackage

        val updateModel = PackageUpdateModel(
            destinationWarehouseId = newDestinationWarehouse.id
        )

        val validationErrors = listOf(
            FieldValidator(
                field = "destinationWarehouseId",
                error = ErrorValidator.InvalidIdPrefix(
                    allowedPrefixes = setOf("WH-")
                )
            )
        )

        coEvery {
            warehouseRepository.findById(input.warehouseId)
        } returns Result.success(warehouse)

        every {
            updatePackageValidator(updateModel)
        } returns ResultValidator.Invalid(validationErrors)

        // When
        val result = useCase(input)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(EntityValidationException::class.java)

        verify(exactly = 1) {
            updatePackageValidator(updateModel)
        }

        coVerify(exactly = 0) {
            packageRepository.update(any(), any())
        }
    }

    @Test
    fun `returns failure when package repository update fails`() = runTest {
        // Given
        every { warehouse.cargoQueue } returns listOf(logisticsPackage)
        every { logisticsPackage.id } returns input.packageId
        every { newDestinationWarehouse.id } returns "WH-654321"

        every {
            logisticsPackage.copy(
                destinationWarehouse = newDestinationWarehouse)
        } returns reroutedPackage

        every { reroutedPackage.id } returns input.packageId

        val updateModel = PackageUpdateModel(
            destinationWarehouseId = newDestinationWarehouse.id
        )

        val exception = RuntimeException("Update failed")

        coEvery {
            warehouseRepository.findById(input.warehouseId)
        } returns Result.success(warehouse)

        every {
            updatePackageValidator(updateModel)
        } returns ResultValidator.Valid

        coEvery {
            packageRepository.update(
                input.packageId,
                updateModel
            )
        } returns Result.failure(exception)

        // When
        val result = useCase(input)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        verify(exactly = 1) {
            updatePackageValidator(updateModel)
        }

        coVerify(exactly = 1) {
            packageRepository.update(
                input.packageId,
                updateModel
            )
        }
    }

    @Test
    fun `returns failure when warehouse repository fails`() = runTest {
        // Given
        val exception = RuntimeException("Warehouse lookup failed")

        coEvery {
            warehouseRepository.findById(input.warehouseId)
        } returns Result.failure(exception)

        // When
        val result = useCase(input)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        verify(exactly = 0) {
            updatePackageValidator(any())
        }

        coVerify(exactly = 0) {
            packageRepository.update(any(), any())
        }
    }
}


