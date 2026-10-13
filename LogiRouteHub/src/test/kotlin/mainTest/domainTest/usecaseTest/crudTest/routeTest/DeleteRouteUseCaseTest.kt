package mainTest.domainTest.usecaseTest.crudTest.routeTest


import com.google.common.truth.Truth.assertThat
import domain.exception.EntityValidationException
import domain.repository.RouteRepository
import domain.usecase.crud.route.DeleteRouteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeleteRouteUseCaseTest {

    @Test
    fun `deletes route when repository delete succeeds`() = runTest {

        // Given
        val routeRepository = mockk<RouteRepository>()
        val routeId = "RT-000001"

        coEvery {
            routeRepository.delete(routeId)
        } returns Result.success(Unit)

        val useCase = DeleteRouteUseCase(routeRepository)

        // When
        val result = useCase(routeId)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(Unit)

        coVerify(exactly = 1) {
            routeRepository.delete(routeId)
        }
    }

    @Test
    fun `returns validation failure when route id is invalid`() = runTest {

        // Given
        val routeRepository = mockk<RouteRepository>()
        val routeId = "WRONG-001"

        val useCase = DeleteRouteUseCase(routeRepository)

        // When
        val result = useCase(routeId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(EntityValidationException::class.java)

        coVerify(exactly = 0) {
            routeRepository.delete(routeId)
        }
    }

    @Test
    fun `returns failure when repository delete fails`() = runTest {

        // Given
        val routeRepository = mockk<RouteRepository>()
        val routeId = "RT-000001"
        val exception = RuntimeException("Failed to delete route")

        coEvery {
            routeRepository.delete(routeId)
        } returns Result.failure(exception)

        val useCase = DeleteRouteUseCase(routeRepository)

        // When
        val result = useCase(routeId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) {
            routeRepository.delete(routeId)
        }
    }
}
