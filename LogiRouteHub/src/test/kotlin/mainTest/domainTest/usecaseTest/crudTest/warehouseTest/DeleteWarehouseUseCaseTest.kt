
package mainTest.domainTest.usecaseTest.crudTest.warehouseTest

import com.google.common.truth.Truth.assertThat
import domain.exception.EntityValidationException
import domain.repository.WarehouseRepository
import domain.usecase.crud.warehouse.DeleteWarehouseUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeleteWarehouseUseCaseTest {

    @Test
    fun `returns success when warehouse id is valid and repository delete succeeds`() = runTest {
        // Given
        val warehouseRepository = mockk<WarehouseRepository>()
        val useCase = DeleteWarehouseUseCase(warehouseRepository)

        val warehouseId = "WH-000001"

        coEvery {
            warehouseRepository.delete(warehouseId)
        } returns Result.success(Unit)

        // When
        val result = useCase(warehouseId)

        // Then
        assertThat(result.isSuccess).isTrue()

        coVerify(exactly = 1) {
            warehouseRepository.delete(warehouseId)
        }
    }

    @Test
    fun `returns failure when repository delete fails`() = runTest {
        // Given
        val warehouseRepository = mockk<WarehouseRepository>()
        val useCase = DeleteWarehouseUseCase(warehouseRepository)

        val warehouseId = "WH-000001"
        val exception = RuntimeException("Failed to delete warehouse")

        coEvery {
            warehouseRepository.delete(warehouseId)
        } returns Result.failure(exception)

        // When
        val result = useCase(warehouseId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) {
            warehouseRepository.delete(warehouseId)
        }
    }

    @Test
    fun `returns validation failure when warehouse id is invalid`() = runTest {
        // Given
        val warehouseRepository = mockk<WarehouseRepository>()
        val useCase = DeleteWarehouseUseCase(warehouseRepository)

        val invalidId = "ABC-000001"

        // When
        val result = useCase(invalidId)

        // Then
        assertThat(result.isFailure).isTrue()

        assertThat(result.exceptionOrNull())
            .isInstanceOf(EntityValidationException::class.java)

        coVerify(exactly = 0) {
            warehouseRepository.delete(any())
        }
    }
}
