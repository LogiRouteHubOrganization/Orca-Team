package mainTest.domainTest.usecaseTest.crudTest.vehicleTest

import com.google.common.truth.Truth.assertThat
import domain.exception.EntityValidationException
import domain.model.Vehicle
import domain.model.Warehouse
import domain.repository.VehicleRepository
import domain.usecase.crud.vehicle.GetVehicleByIdUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetVehicleByIdUseCaseTest {

    @Test
    fun `returns vehicle when id is valid and vehicle exists`() = runTest {
        // Given
        val vehicleRepository = mockk<VehicleRepository>()
        val useCase = GetVehicleByIdUseCase(vehicleRepository)
        val vehicle = createTestVehicle("TRK-000001")

        coEvery {
            vehicleRepository.findById("TRK-000001")
        } returns Result.success(vehicle)

        // When
        val result = useCase("TRK-000001")

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(vehicle)

        coVerify(exactly = 1) {
            vehicleRepository.findById("TRK-000001")
        }
    }

    @Test
    fun `returns null when id is valid but vehicle does not exist`() = runTest {
        // Given
        val vehicleRepository = mockk<VehicleRepository>()
        val useCase = GetVehicleByIdUseCase(vehicleRepository)

        coEvery {
            vehicleRepository.findById("TRK-000001")
        } returns Result.success(null)

        // When
        val result = useCase("TRK-000001")

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNull()

        coVerify(exactly = 1) {
            vehicleRepository.findById("TRK-000001")
        }
    }

    @Test
    fun `returns failure when repository findById fails`() = runTest {
        // Given
        val vehicleRepository = mockk<VehicleRepository>()
        val useCase = GetVehicleByIdUseCase(vehicleRepository)
        val exception = RuntimeException("Database error")

        coEvery {
            vehicleRepository.findById("TRK-000001")
        } returns Result.failure(exception)

        // When
        val result = useCase("TRK-000001")

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) {
            vehicleRepository.findById("TRK-000001")
        }
    }

    @Test
    fun `returns validation failure when vehicle id is invalid`() = runTest {
        // Given
        val vehicleRepository = mockk<VehicleRepository>()
        val useCase = GetVehicleByIdUseCase(vehicleRepository)
        val invalidId = "AV-000001"

        // When
        val result = useCase(invalidId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(EntityValidationException::class.java)

        coVerify(exactly = 0) {
            vehicleRepository.findById(any())
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

    private fun createTestVehicle(id: String = "TRK-000001"): Vehicle {
        return Vehicle(
            id = id,
            maxCapacityKg = 10000.0,
            costPerKm = 2.5,
            currentWarehouse = createWarehouse()
        )
    }
}
