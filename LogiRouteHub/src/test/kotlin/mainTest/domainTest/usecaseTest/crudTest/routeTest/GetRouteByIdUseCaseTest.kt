package mainTest.domainTest.usecaseTest.crudTest.routeTest

import com.google.common.truth.Truth.assertThat
import domain.exception.EntityValidationException
import domain.model.Route
import domain.model.Warehouse
import domain.repository.RouteRepository
import domain.usecase.crud.route.GetRouteByIdUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetRouteByIdUseCaseTest {

    @Test
    fun `returns route when repository finds route`() = runTest {

        // Given
        val routeRepository = mockk<RouteRepository>()
        val route = createTestRoute()

        coEvery {
            routeRepository.findById(route.id)
        } returns Result.success(route)

        val useCase = GetRouteByIdUseCase(routeRepository)

        // When
        val result = useCase(route.id)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(route)

        coVerify(exactly = 1) {
            routeRepository.findById(route.id)
        }
    }

    @Test
    fun `returns validation failure when route id is invalid`() = runTest {

        // Given
        val routeRepository = mockk<RouteRepository>()
        val routeId = "WRONG-001"

        val useCase = GetRouteByIdUseCase(routeRepository)

        // When
        val result = useCase(routeId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(EntityValidationException::class.java)

        coVerify(exactly = 0) {
            routeRepository.findById(routeId)
        }
    }

    @Test
    fun `returns failure when repository find fails`() = runTest {

        // Given
        val routeRepository = mockk<RouteRepository>()
        val routeId = "RT-000001"
        val exception = RuntimeException("Failed to find route")

        coEvery {
            routeRepository.findById(routeId)
        } returns Result.failure(exception)

        val useCase = GetRouteByIdUseCase(routeRepository)

        // When
        val result = useCase(routeId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) {
            routeRepository.findById(routeId)
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

    private fun createTestRoute(): Route {
        return Route(
            id = "RT-000001",
            originWarehouse = createWarehouse("WH-000001"),
            destinationWarehouse = createWarehouse("WH-000002"),
            distanceKm = 10.0,
            typicalDelayMin = 5
        )
    }
}
