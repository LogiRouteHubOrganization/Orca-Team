package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth
import domain.algorithm.greedy.VehicleCoverage
import domain.algorithm.greedy.VehicleCoverageMapper
import domain.model.Route
import domain.model.Vehicle
import domain.repository.RouteRepository
import domain.repository.VehicleRepository
import domain.usecase.GetGreedyDispatchDataUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetGreedyDispatchDataUseCaseTest {

    @Test
    fun `returns success with vehicle coverage list when repositories succeed`() = runTest {

        val vehicleRepository = mockk<VehicleRepository>()
        val routeRepository = mockk<RouteRepository>()
        val greedyInputPreparer = mockk<VehicleCoverageMapper>()

        val vehiclesList = listOf(mockk<Vehicle>(relaxed = true))
        val routesList = listOf(mockk<Route>(relaxed = true))
        val expectedCoverage = listOf(mockk<VehicleCoverage>(relaxed = true))

        coEvery { vehicleRepository.findAll() } returns Result.success(vehiclesList)
        coEvery { routeRepository.findAll() } returns Result.success(routesList)
        coEvery { greedyInputPreparer.prepareVehicleCoverage(vehiclesList, routesList) } returns expectedCoverage

        val useCase = GetGreedyDispatchDataUseCase(
            vehicleRepository,
            routeRepository,
            greedyInputPreparer
        )


        val result = useCase()


        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.getOrNull()).isEqualTo(expectedCoverage)

        coVerify(exactly = 1) { vehicleRepository.findAll() }
        coVerify(exactly = 1) { routeRepository.findAll() }
        coVerify(exactly = 1) { greedyInputPreparer.prepareVehicleCoverage(vehiclesList, routesList) }
    }

    @Test
    fun `returns failure when vehicle repository fails`() = runTest {
        // Given
        val vehicleRepository = mockk<VehicleRepository>()
        val routeRepository = mockk<RouteRepository>()
        val greedyInputPreparer = mockk<VehicleCoverageMapper>()

        val exception = RuntimeException("Failed to fetch vehicles")

        coEvery { vehicleRepository.findAll() } returns Result.failure(exception)

        val useCase = GetGreedyDispatchDataUseCase(
            vehicleRepository,
            routeRepository,
            greedyInputPreparer
        )


        val result = useCase()


        Truth.assertThat(result.isFailure).isTrue()
        Truth.assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) { vehicleRepository.findAll() }
        coVerify(exactly = 0) { routeRepository.findAll() }
        coVerify(exactly = 0) { greedyInputPreparer.prepareVehicleCoverage(any(), any()) }
    }

    @Test
    fun `returns failure when route repository fails`() = runTest {
        // Given
        val vehicleRepository = mockk<VehicleRepository>()
        val routeRepository = mockk<RouteRepository>()
        val greedyInputPreparer = mockk<VehicleCoverageMapper>()

        val vehiclesList = listOf(mockk<Vehicle>(relaxed = true))
        val exception = RuntimeException("Failed to fetch routes")

        coEvery { vehicleRepository.findAll() } returns Result.success(vehiclesList)
        coEvery { routeRepository.findAll() } returns Result.failure(exception)

        val useCase = GetGreedyDispatchDataUseCase(
            vehicleRepository,
            routeRepository,
            greedyInputPreparer
        )


        val result = useCase()


        Truth.assertThat(result.isFailure).isTrue()
        Truth.assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) { vehicleRepository.findAll() }
        coVerify(exactly = 1) { routeRepository.findAll() }
        coVerify(exactly = 0) { greedyInputPreparer.prepareVehicleCoverage(any(), any()) }
    }
}
