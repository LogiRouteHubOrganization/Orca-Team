package mainTest.domainTest.usecaseTest.crudTest.vehicleTest

import com.google.common.truth.Truth.assertThat
import domain.exception.EntityValidationException
import domain.model.Vehicle
import domain.model.Warehouse
import domain.model.updateinput.VehicleUpdateModel
import domain.repository.VehicleRepository
import domain.usecase.crud.vehicle.UpdateVehicleUseCase
import domain.validator.vehicle.UpdateVehicleValidator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateVehicleUseCaseTest {

    @Test
    fun `returns updated vehicle when id and update data are valid`() = runTest {
        // Given
        val vehicleRepository = mockk<VehicleRepository>()
        val vehicleValidator = UpdateVehicleValidator()

        val useCase = UpdateVehicleUseCase(
            vehicleRepository = vehicleRepository,
            vehicleValidator = vehicleValidator
        )

        val updateModel = VehicleUpdateModel(maxCapacityKg = 12000.0)

        val updatedVehicle = createTestVehicle("TRK-000001", maxCapacityKg = 12000.0)

        coEvery {
            vehicleRepository.update("TRK-000001", updateModel)
        } returns Result.success(updatedVehicle)

        // When
        val result = useCase(id = "TRK-000001",
            updateRequest = updateModel
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(updatedVehicle)

        coVerify(exactly = 1) {
            vehicleRepository.update("TRK-000001", updateModel)
        }
    }

    @Test
    fun `returns failure when repository update fails`() = runTest {
        // Given
        val vehicleRepository = mockk<VehicleRepository>()
        val vehicleValidator = UpdateVehicleValidator()

        val useCase = UpdateVehicleUseCase(
            vehicleRepository = vehicleRepository,
            vehicleValidator = vehicleValidator
        )

        val updateModel = VehicleUpdateModel(maxCapacityKg = 12000.0)

        val exception = RuntimeException("Failed to update vehicle")

        coEvery {
            vehicleRepository.update("TRK-000001", updateModel)
        } returns Result.failure(exception)

        // When
        val result = useCase(id = "TRK-000001",
            updateRequest = updateModel
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) {
            vehicleRepository.update("TRK-000001", updateModel)
        }
    }

    @Test
    fun `returns validation failure when vehicle id is invalid`() = runTest {
        // Given
        val vehicleRepository = mockk<VehicleRepository>()
        val vehicleValidator = UpdateVehicleValidator()

        val useCase = UpdateVehicleUseCase(
            vehicleRepository = vehicleRepository,
            vehicleValidator = vehicleValidator
        )

        val updateModel = VehicleUpdateModel(maxCapacityKg = 12000.0)

        // When
        val result = useCase(id = "ABC-000001",
            updateRequest = updateModel
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(EntityValidationException::class.java)

        coVerify(exactly = 0) {
            vehicleRepository.update(any(), any())
        }
    }


    private fun createWarehouse(id: String = "WH-000001"): Warehouse {
        return Warehouse(
            id = id,
            name = "Test Warehouse",
            regionalZone = "NORTH",
            latitude = 31.5,
            longitude = 34.5
        )
    }

    private fun createTestVehicle(
        id: String = "TRK-000001",
        maxCapacityKg: Double = 10000.0
    ): Vehicle {
        return Vehicle(
            id = id,
            maxCapacityKg = maxCapacityKg,
            costPerKm = 2.5,
            currentWarehouse = createWarehouse()
        )
    }
}
