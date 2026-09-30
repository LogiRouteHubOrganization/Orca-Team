package mainTest.domainTest.usecaseTest.crudTest.vehicle

import com.google.common.truth.Truth.assertThat
import domain.model.Vehicle
import domain.model.Warehouse
import domain.repository.VehicleRepository
import domain.usecase.crud.vehicle.CreateVehicleUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CreateVehicleUseCaseTest {

    @Test
    fun `returns created vehicle when repository add succeeds`() = runTest {
        // Given
        val vehicleRepository = mockk<VehicleRepository>()
        val vehicle = createTestVehicle("TRK-000001")

        coEvery {
            vehicleRepository.add(vehicle)
        } returns Result.success(vehicle)

        val useCase = CreateVehicleUseCase(vehicleRepository)

        // When
        val result = useCase(vehicle)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(vehicle)

        coVerify(exactly = 1) {
            vehicleRepository.add(vehicle)
        }
    }

    @Test
    fun `returns failure when repository add fails`() = runTest {
        // Given
        val vehicleRepository = mockk<VehicleRepository>()
        val vehicle = createTestVehicle("TRK-000001")
        val exception = RuntimeException("Failed to create vehicle")

        coEvery {
            vehicleRepository.add(vehicle)
        } returns Result.failure(exception)

        val useCase = CreateVehicleUseCase(vehicleRepository)

        // When
        val result = useCase(vehicle)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) {
            vehicleRepository.add(vehicle)
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
            maxCapacityKg = 1500.0,
            costPerKm = 3.5,
            currentWarehouse = createWarehouse()
        )
    }
}
