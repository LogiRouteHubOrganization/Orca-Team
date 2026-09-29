package main.domain.usecase.crud.vehicle

import com.google.common.truth.Truth.assertThat
import domain.exception.EntityValidationException
import domain.repository.VehicleRepository
import domain.usecase.crud.vehicle.DeleteVehicleUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeleteVehicleUseCaseTest {

    @Test
    fun `returns success when vehicle id is valid and repository delete succeeds`() = runTest {
        // Given
        val vehicleRepository = mockk<VehicleRepository>()
        val useCase = DeleteVehicleUseCase(vehicleRepository)
        val vehicleId = "TRK-000001"

        coEvery {
            vehicleRepository.delete(vehicleId)
        } returns Result.success(Unit)

        // When
        val result = useCase(vehicleId)

        // Then
        assertThat(result.isSuccess).isTrue()

        coVerify(exactly = 1) {
            vehicleRepository.delete(vehicleId)
        }
    }

    @Test
    fun `returns failure when repository delete fails`() = runTest {
        // Given
        val vehicleRepository = mockk<VehicleRepository>()
        val useCase = DeleteVehicleUseCase(vehicleRepository)
        val vehicleId = "TRK-000001"
        val exception = RuntimeException("Failed to delete vehicle")

        coEvery {
            vehicleRepository.delete(vehicleId)
        } returns Result.failure(exception)

        // When
        val result = useCase(vehicleId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) {
            vehicleRepository.delete(vehicleId)
        }
    }

    @Test
    fun `returns validation failure when vehicle id is invalid`() = runTest {
        // Given
        val vehicleRepository = mockk<VehicleRepository>()
        val useCase = DeleteVehicleUseCase(vehicleRepository)
        val invalidId = "VEH-000001"

        // When
        val result = useCase(invalidId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(EntityValidationException::class.java)

        coVerify(exactly = 0) {
            vehicleRepository.delete(any())
        }
    }
}
