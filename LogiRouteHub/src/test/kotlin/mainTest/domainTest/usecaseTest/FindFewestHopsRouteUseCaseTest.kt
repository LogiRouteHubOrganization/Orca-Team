
package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Route
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.usecase.FindFewestHopsRouteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class FindFewestHopsRouteUseCaseTest {

    private val warehouseRepository = mockk<WarehouseRepository>()

    private val useCase = FindFewestHopsRouteUseCase(
        warehouseRepository = warehouseRepository
    )

    private val startWarehouse = Warehouse(
        id = "WH-000001",
        name = "Start Warehouse",
        regionalZone = "North",
        latitude = 31.9,
        longitude = 35.2
    )

    private val destinationWarehouse = Warehouse(
        id = "WH-000002",
        name = "Destination Warehouse",
        regionalZone = "South",
        latitude = 31.5,
        longitude = 34.5
    )

    @Test
    fun `should return least hop path when both warehouses exist`() = runTest {
        // Given
        val route = Route(
            id = "RT-000001",
            originWarehouse = startWarehouse,
            destinationWarehouse = destinationWarehouse,
            distanceKm = 10.0,
            typicalDelayMin = 5
        )

        startWarehouse.addRoute(route)

        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        coEvery {
            warehouseRepository.findById("WH-000002")
        } returns Result.success(destinationWarehouse)

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000002"
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).containsExactly(
            startWarehouse,
            destinationWarehouse
        ).inOrder()
    }


    @Test
    fun `should return empty list when start warehouse does not exist`() = runTest {
        // Given
        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(null)

        coEvery {
            warehouseRepository.findById("WH-000002")
        } returns Result.success(destinationWarehouse)

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000002"
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEmpty()

        coVerify(exactly = 1) {
            warehouseRepository.findById("WH-000001")
            warehouseRepository.findById("WH-000002")
        }
    }



    @Test
    fun `should return empty list when destination warehouse does not exist`() = runTest {
        // Given
        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        coEvery {
            warehouseRepository.findById("WH-000002")
        } returns Result.success(null)

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000002"
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEmpty()

        coVerify(exactly = 1) {
            warehouseRepository.findById("WH-000001")
            warehouseRepository.findById("WH-000002")
        }
    }


    @Test
    fun `should return failure when warehouse repository fails`() = runTest {
        // Given
        val exception = RuntimeException("Repository error")

        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.failure(exception)

        coEvery {
            warehouseRepository.findById("WH-000002")
        } returns Result.success(destinationWarehouse)

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000002"
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message)
            .isEqualTo("Repository error")

        coVerify(exactly = 1) {
            warehouseRepository.findById("WH-000001")
            warehouseRepository.findById("WH-000002")
        }
    }


    @Test
    fun `should choose path with fewer hops when multiple paths exist`() = runTest {
        setupMultiplePathsScenario()

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000002"
        )

        // Then
        assertThat(result.getOrNull()).containsExactly(
            startWarehouse,
            destinationWarehouse
        ).inOrder()
    }

    @Test
    fun `should start both warehouse lookups concurrently`() = runTest {
        // Given
        val startLookupStarted = CompletableDeferred<Unit>()
        val destinationLookupStarted = CompletableDeferred<Unit>()
        val allowLookupsToFinish = CompletableDeferred<Unit>()

        coEvery {
            warehouseRepository.findById("WH-000001")
        } coAnswers {
            startLookupStarted.complete(Unit)
            allowLookupsToFinish.await()
            Result.success(startWarehouse)
        }

        coEvery {
            warehouseRepository.findById("WH-000002")
        } coAnswers {
            destinationLookupStarted.complete(Unit)
            allowLookupsToFinish.await()
            Result.success(destinationWarehouse)
        }

        // When
        val resultDeferred = async {
            useCase(
                startWarehouseId = "WH-000001",
                destinationWarehouseId = "WH-000002"
            )
        }

        startLookupStarted.await()
        destinationLookupStarted.await()

        allowLookupsToFinish.complete(Unit)

        val result = resultDeferred.await()

        // Then
        assertThat(result.isSuccess).isTrue()

        coVerify(exactly = 1) {
            warehouseRepository.findById("WH-000001")
        }
        coVerify(exactly = 1) {
            warehouseRepository.findById("WH-000002")
        }
    }

    @Test
    fun `should scatter caller cancellation to both warehouse lookups`() = runTest {
        // Given
        val startLookupStarted = CompletableDeferred<Unit>()
        val destinationLookupStarted = CompletableDeferred<Unit>()
        val startLookupCancelled = CompletableDeferred<Unit>()
        val destinationLookupCancelled = CompletableDeferred<Unit>()

        coEvery {
            warehouseRepository.findById("WH-000001")
        } coAnswers {
            startLookupStarted.complete(Unit)

            try {
                awaitCancellation()
            } finally {
                startLookupCancelled.complete(Unit)
            }
        }

        coEvery {
            warehouseRepository.findById("WH-000002")
        } coAnswers {
            destinationLookupStarted.complete(Unit)

            try {
                awaitCancellation()
            } finally {
                destinationLookupCancelled.complete(Unit)
            }
        }

        // When
        val routeJob = async {
            useCase(
                startWarehouseId = "WH-000001",
                destinationWarehouseId = "WH-000002"
            )
        }

        startLookupStarted.await()
        destinationLookupStarted.await()
        routeJob.cancelAndJoin()

        val cancellationResult = runCatching {
            routeJob.await()
        }

        // Then
        assertThat(routeJob.isCancelled).isTrue()
        assertThat(cancellationResult.isFailure).isTrue()
        assertThat(cancellationResult.exceptionOrNull())
            .isInstanceOf(CancellationException::class.java)

        assertThat(startLookupCancelled.isCompleted).isTrue()
        assertThat(destinationLookupCancelled.isCompleted).isTrue()
    }

    @Test
    fun `should cancel destination lookup when start lookup fails`() = runTest {
        // Given
        val destinationLookupStarted = CompletableDeferred<Unit>()
        val allowStartFailure = CompletableDeferred<Unit>()
        val destinationLookupCancelled = CompletableDeferred<Unit>()

        val expectedException = RuntimeException("Start warehouse lookup failed")

        coEvery {
            warehouseRepository.findById("WH-000001")
        } coAnswers {
            allowStartFailure.await()
            throw expectedException
        }

        coEvery {
            warehouseRepository.findById("WH-000002")
        } coAnswers {
            destinationLookupStarted.complete(Unit)

            try {
                awaitCancellation()
            } finally {
                destinationLookupCancelled.complete(Unit)
            }
        }

        // When
        val resultDeferred = async {
            useCase(
                startWarehouseId = "WH-000001",
                destinationWarehouseId = "WH-000002"
            )
        }

        destinationLookupStarted.await()
        allowStartFailure.complete(Unit)

        val result = resultDeferred.await()

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(RuntimeException::class.java)
        assertThat(result.exceptionOrNull()?.message)
            .isEqualTo("Start warehouse lookup failed")

        assertThat(destinationLookupCancelled.isCompleted).isTrue()

        coVerify(exactly = 1) {
            warehouseRepository.findById("WH-000001")
        }
        coVerify(exactly = 1) {
            warehouseRepository.findById("WH-000002")
        }
    }




    private fun setupMultiplePathsScenario() {
        val intermediateWarehouse = Warehouse(
            id = "WH-000003",
            name = "Intermediate",
            regionalZone = "Central",
            latitude = 31.7,
            longitude = 35.0
        )
        val farWarehouse = Warehouse(
            id = "WH-000004",
            name = "Far",
            regionalZone = "West",
            latitude = 31.6,
            longitude = 34.8
        )

        startWarehouse.addRoute(Route(
            "RT-000001",
            startWarehouse,
            destinationWarehouse,
            100.0, 10))

        startWarehouse.addRoute(Route(
            "RT-000002",
            startWarehouse,
            intermediateWarehouse,
            5.0, 3))

        intermediateWarehouse.addRoute(Route(
            "RT-000003",
            intermediateWarehouse,
            farWarehouse,
            5.0, 3))

        farWarehouse.addRoute(Route(
            "RT-000004",
            farWarehouse,
            destinationWarehouse,
            5.0, 3))

        coEvery { warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        coEvery { warehouseRepository.findById("WH-000002")
        } returns Result.success(destinationWarehouse)
    }

    @Test
    fun `should return path with correct order when multiple hops are required`() = runTest {
        val (secondWarehouse, thirdWarehouse) = setupMultiHopScenario()

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000002"
        )

        // Then
        assertThat(result.getOrNull()).containsExactly(
            startWarehouse,
            secondWarehouse,
            thirdWarehouse,
            destinationWarehouse
        ).inOrder()
    }

    private fun setupMultiHopScenario(): Pair<Warehouse, Warehouse> {
        val secondWarehouse = Warehouse(
            id = "WH-000003",
            name = "Second Warehouse",
            regionalZone = "Central",
            latitude = 31.7,
            longitude = 35.0
        )
        val thirdWarehouse = Warehouse(
            id = "WH-000004",
            name = "Third Warehouse",
            regionalZone = "West",
            latitude = 31.6,
            longitude = 34.8
        )

        startWarehouse.addRoute(Route(
            "RT-000001",
            startWarehouse,
            secondWarehouse,
            5.0, 3))
        secondWarehouse.addRoute(Route(
            "RT-000002",
            secondWarehouse,
            thirdWarehouse,
            7.0, 4))
        thirdWarehouse.addRoute(Route(
            "RT-000003",
            thirdWarehouse,
            destinationWarehouse,
            8.0, 5))

        coEvery { warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        coEvery { warehouseRepository.findById("WH-000002")
        } returns Result.success(destinationWarehouse)

        return Pair(secondWarehouse, thirdWarehouse)
    }

    @Test
    fun `should return empty list when destination is unreachable`() = runTest {
        // Given
        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        coEvery {
            warehouseRepository.findById("WH-000002")
        } returns Result.success(destinationWarehouse)

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000002"
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEmpty()
    }

    @Test
    fun `should return start warehouse when start and destination are the same`() = runTest {
        // Given
        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000001"
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).containsExactly(
            startWarehouse
        )
    }

    @Test
    fun `should handle cycle without infinite traversal`() = runTest {
        val middleWarehouse = setupCycleScenario()

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000002"
        )

        // Then
        assertThat(result.getOrNull()).containsExactly(
            startWarehouse,
            middleWarehouse,
            destinationWarehouse
        ).inOrder()
    }

    private fun setupCycleScenario(): Warehouse {
        val middleWarehouse = Warehouse(
            id = "WH-000003", name = "Middle", regionalZone = "Central", latitude = 31.7, longitude = 35.0
        )

        startWarehouse.addRoute(Route(
            "RT-000001",
            startWarehouse,
            middleWarehouse,
            5.0, 3))

        middleWarehouse.addRoute(Route(
            "RT-000002",
            middleWarehouse,
            startWarehouse,
            5.0, 3))

        middleWarehouse.addRoute(Route(
            "RT-000003",
            middleWarehouse,
            destinationWarehouse,
            5.0, 3))

        coEvery { warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        coEvery { warehouseRepository.findById("WH-000002")
        } returns Result.success(destinationWarehouse)

        return middleWarehouse
    }
}

