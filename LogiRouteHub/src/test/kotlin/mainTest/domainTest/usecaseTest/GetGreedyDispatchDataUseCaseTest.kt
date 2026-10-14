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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
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

        coEvery {
            vehicleRepository.findAll()
        } returns Result.failure(exception)

        coEvery {
            routeRepository.findAll()
        } returns Result.success(emptyList())

        val useCase = GetGreedyDispatchDataUseCase(
            vehicleRepository,
            routeRepository,
            greedyInputPreparer
        )

        // When
        val result = useCase()

        // Then
        Truth.assertThat(result.isFailure).isTrue()
        Truth.assertThat(result.exceptionOrNull()?.message)
            .isEqualTo("Failed to fetch vehicles")

        coVerify(exactly = 1) {
            vehicleRepository.findAll()
        }
        coVerify(exactly = 0) {
            greedyInputPreparer.prepareVehicleCoverage(any(), any())
        }
    }


    @Test
    fun `returns failure when route repository fails`() = runTest {
        // Given
        val vehicleRepository = mockk<VehicleRepository>()
        val routeRepository = mockk<RouteRepository>()
        val greedyInputPreparer = mockk<VehicleCoverageMapper>()

        val vehiclesList = listOf(mockk<Vehicle>(relaxed = true))
        val exception = RuntimeException("Failed to fetch routes")

        coEvery {
            vehicleRepository.findAll()
        } returns Result.success(vehiclesList)

        coEvery {
            routeRepository.findAll()
        } returns Result.failure(exception)

        val useCase = GetGreedyDispatchDataUseCase(
            vehicleRepository,
            routeRepository,
            greedyInputPreparer
        )

        // When
        val result = useCase()

        // Then
        Truth.assertThat(result.isFailure).isTrue()
        Truth.assertThat(result.exceptionOrNull()?.message)
            .isEqualTo("Failed to fetch routes")

        coVerify(exactly = 1) {
            vehicleRepository.findAll()
            routeRepository.findAll()
        }
        coVerify(exactly = 0) {
            greedyInputPreparer.prepareVehicleCoverage(any(), any())
        }
    }


    @Test
    fun `starts vehicle and route lookups concurrently`() = runTest {
        // Given
        val vehicleRepository = mockk<VehicleRepository>()
        val routeRepository = mockk<RouteRepository>()
        val greedyInputPreparer = mockk<VehicleCoverageMapper>()

        val vehiclesList = listOf(mockk<Vehicle>(relaxed = true))
        val routesList = listOf(mockk<Route>(relaxed = true))
        val expectedCoverage = listOf(mockk<VehicleCoverage>(relaxed = true))

        val vehicleLookupStarted = CompletableDeferred<Unit>()
        val routeLookupStarted = CompletableDeferred<Unit>()
        val allowLookupsToFinish = CompletableDeferred<Unit>()

        coEvery {
            vehicleRepository.findAll()
        } coAnswers {
            vehicleLookupStarted.complete(Unit)
            allowLookupsToFinish.await()
            Result.success(vehiclesList)
        }

        coEvery {
            routeRepository.findAll()
        } coAnswers {
            routeLookupStarted.complete(Unit)
            allowLookupsToFinish.await()
            Result.success(routesList)
        }

        coEvery {
            greedyInputPreparer.prepareVehicleCoverage(vehiclesList, routesList)
        } returns expectedCoverage

        val useCase = GetGreedyDispatchDataUseCase(
            vehicleRepository,
            routeRepository,
            greedyInputPreparer
        )

        // When
        val resultDeferred = async { useCase() }
        vehicleLookupStarted.await()
        routeLookupStarted.await()
        allowLookupsToFinish.complete(Unit)
        val result = resultDeferred.await()

        // Then
        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.getOrNull()).isEqualTo(expectedCoverage)

        coVerify(exactly = 1) {
            vehicleRepository.findAll()
        }
        coVerify(exactly = 1) {
            routeRepository.findAll()
        }
        coVerify(exactly = 1) {
            greedyInputPreparer.prepareVehicleCoverage(vehiclesList, routesList)
        }
    }


    @Test
    fun `cancels route lookup when vehicle lookup fails`() = runTest {
        // Given
        val vehicleRepository = mockk<VehicleRepository>()
        val routeRepository = mockk<RouteRepository>()
        val greedyInputPreparer = mockk<VehicleCoverageMapper>()

        val vehicleLookupStarted = CompletableDeferred<Unit>()
        val routeLookupStarted = CompletableDeferred<Unit>()
        val allowVehicleFailure = CompletableDeferred<Unit>()
        val routeLookupCancelled = CompletableDeferred<Unit>()

        val exception = RuntimeException("Failed to fetch vehicles")

        coEvery {
            vehicleRepository.findAll()
        } coAnswers {
            vehicleLookupStarted.complete(Unit)
            allowVehicleFailure.await()
            Result.failure(exception)
        }

        coEvery {
            routeRepository.findAll()
        } coAnswers {
            routeLookupStarted.complete(Unit)

            try {
                awaitCancellation()
            } finally {
                routeLookupCancelled.complete(Unit)
            }
        }

        val useCase = GetGreedyDispatchDataUseCase(
            vehicleRepository,
            routeRepository,
            greedyInputPreparer
        )

        // When
        val resultDeferred = async { useCase() }
        vehicleLookupStarted.await()
        routeLookupStarted.await()
        allowVehicleFailure.complete(Unit)
        val result = resultDeferred.await()

        // Then
        Truth.assertThat(result.isFailure).isTrue()
        Truth.assertThat(result.exceptionOrNull()?.message)
            .isEqualTo("Failed to fetch vehicles")

        Truth.assertThat(routeLookupCancelled.isCompleted).isTrue()

        coVerify(exactly = 1) {
            vehicleRepository.findAll()
            routeRepository.findAll()
        }
        coVerify(exactly = 0) {
            greedyInputPreparer.prepareVehicleCoverage(any(), any())
        }
    }


    @Test
    fun `scatter caller cancellation to both repository lookups`() = runTest {
        // Given
        val vehicleRepository = mockk<VehicleRepository>()
        val routeRepository = mockk<RouteRepository>()
        val greedyInputPreparer = mockk<VehicleCoverageMapper>()

        val vehicleLookupStarted = CompletableDeferred<Unit>()
        val routeLookupStarted = CompletableDeferred<Unit>()
        val vehicleLookupCancelled = CompletableDeferred<Unit>()
        val routeLookupCancelled = CompletableDeferred<Unit>()

        coEvery {
            vehicleRepository.findAll()
        } coAnswers {
            vehicleLookupStarted.complete(Unit)

            try {
                awaitCancellation()
            } finally {
                vehicleLookupCancelled.complete(Unit)
            }
        }

        coEvery {
            routeRepository.findAll()
        } coAnswers {
            routeLookupStarted.complete(Unit)

            try {
                awaitCancellation()
            } finally {
                routeLookupCancelled.complete(Unit)
            }
        }

        val useCase = GetGreedyDispatchDataUseCase(
            vehicleRepository,
            routeRepository,
            greedyInputPreparer
        )

        // When
        val dispatchJob = async { useCase() }
        vehicleLookupStarted.await()
        routeLookupStarted.await()
        dispatchJob.cancelAndJoin()

        val cancellationResult = runCatching { dispatchJob.await() }

        // Then
        Truth.assertThat(dispatchJob.isCancelled).isTrue()

        Truth.assertThat(cancellationResult.isFailure).isTrue()
        Truth.assertThat(cancellationResult.exceptionOrNull())
            .isInstanceOf(CancellationException::class.java)

        Truth.assertThat(vehicleLookupCancelled.isCompleted).isTrue()
        Truth.assertThat(routeLookupCancelled.isCompleted).isTrue()

        coVerify(exactly = 1) {
            vehicleRepository.findAll()
            routeRepository.findAll()
        }
    }


}
