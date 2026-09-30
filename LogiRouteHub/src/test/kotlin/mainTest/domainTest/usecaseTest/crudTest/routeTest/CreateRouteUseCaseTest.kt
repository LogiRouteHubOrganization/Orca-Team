package mainTest.domainTest.usecaseTest.crudTest.routeTest

import com.google.common.truth.Truth.assertThat
import domain.model.Route
import domain.model.Warehouse
import domain.repository.RouteRepository
import domain.usecase.crud.route.CreateRouteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CreateRouteUseCaseTest {

    @Test
    fun `returns created route when repository add succeeds`() = runTest {

        // Given
        val routeRepository = mockk<RouteRepository>()
        val route = createTestRoute()

        coEvery {
            routeRepository.add(route)
        } returns Result.success(route)

        val useCase = CreateRouteUseCase(routeRepository)

        // When
        val result = useCase(route)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(route)

        coVerify(exactly = 1) {
            routeRepository.add(route)
        }
    }

    @Test
    fun `returns failure when repository add fails`() = runTest {

        // Given
        val routeRepository = mockk<RouteRepository>()
        val route = createTestRoute()
        val exception = RuntimeException("Failed to create route")

        coEvery {
            routeRepository.add(route)
        } returns Result.failure(exception)

        val useCase = CreateRouteUseCase(routeRepository)

        // When
        val result = useCase(route)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) {
            routeRepository.add(route)
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