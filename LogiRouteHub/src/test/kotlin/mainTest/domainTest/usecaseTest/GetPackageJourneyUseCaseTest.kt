package mainTest.domainTest.usecaseTest
import com.google.common.truth.Truth.assertThat
import domain.model.Package
import domain.model.Route
import domain.model.Warehouse
import domain.repository.PackageRepository
import domain.routing.ShortestPathRouter
import domain.usecase.GetPackageJourneyUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetPackageJourneyUseCaseTest {

    private val packageRepository = mockk<PackageRepository>()

    private val shortestPathRouter = mockk<ShortestPathRouter>()

    private val useCase = GetPackageJourneyUseCase(
        packageRepository = packageRepository,
        shortestPathRouter = shortestPathRouter
    )

    @Test
    fun `returns package journey when package exists`() = runTest {
        // Given
        val warehouseA = createWarehouse("WH-000001")
        val warehouseB = createWarehouse("WH-000002")
        val warehouseC = createWarehouse("WH-000003")

        val routeAB = createRoute(
            id = "RT-000001",
            origin = warehouseA,
            destination = warehouseB,
            distanceKm = 10.0,
            delayMin = 5
        )

        val routeBC = createRoute(
            id = "RT-000002",
            origin = warehouseB,
            destination = warehouseC,
            distanceKm = 20.0,
            delayMin = 10
        )
        warehouseA.addRoute(routeAB)
        warehouseB.addRoute(routeBC)

        val logisticsPackage = createPackage(
            id = "PKG-000001",
            origin = warehouseA,
            destination = warehouseC
        )
        coEvery {
            packageRepository.findById("PKG-000001")
        } returns Result.success(logisticsPackage)

        every {
            shortestPathRouter.findShortestPath(
                startWarehouse = warehouseA,
                destinationWarehouse = warehouseC
            )
        } returns listOf(
            warehouseA,
            warehouseB,
            warehouseC
        )
        // When
        val result = useCase("PKG-000001")

        // Then
        assertThat(result.isSuccess).isTrue()

        val journey = result.getOrNull()

        assertThat(journey).isNotNull()
        assertThat(journey!!.packageId).isEqualTo("PKG-000001")

        assertThat(journey.originWarehouse).isEqualTo(warehouseA)
        assertThat(journey.destinationWarehouse).isEqualTo(warehouseC)
        assertThat(journey.warehouses).containsExactly(
                warehouseA, warehouseB, warehouseC).inOrder()

        assertThat(journey.routes).containsExactly(routeAB, routeBC).inOrder()

        assertThat(journey.totalDistanceKm).isEqualTo(30.0)

        assertThat(journey.totalTypicalDelayMin).isEqualTo(15)

        coVerify(exactly = 1) {
            packageRepository.findById("PKG-000001")
        }

        verify(exactly = 1) {
            shortestPathRouter.findShortestPath(
                startWarehouse = warehouseA,
                destinationWarehouse = warehouseC
            )
        }
    }

    @Test
    fun `returns success with null when package does not exist`() = runTest {
        // Given
        coEvery {
            packageRepository.findById("PKG-000001")
        } returns Result.success(null)

        // When
        val result = useCase("PKG-000001")

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNull()

        coVerify(exactly = 1) {
            packageRepository.findById("PKG-000001")
        }

        verify(exactly = 0) {
            shortestPathRouter.findShortestPath(
                any(), any()
            )
        }
    }

    @Test
    fun `returns failure when package repository fails`() = runTest {
        // Given
        val exception = Exception("Repository error")

        coEvery {
            packageRepository.findById("PKG-000001")
        } returns Result.failure(exception)

        // When
        val result = useCase("PKG-000001")

        // Then
        assertThat(result.isFailure).isTrue()

        assertThat(result.exceptionOrNull())
            .isEqualTo(exception)

        coVerify(exactly = 1) {
            packageRepository.findById("PKG-000001")
        }

        verify(exactly = 0) {
            shortestPathRouter.findShortestPath(
                any(),
                any()
            )
        }
    }

    private fun createWarehouse(id: String): Warehouse {
        return Warehouse(
            id = id,
            name = "Test Warehouse",
            regionalZone = "Test Zone",
            latitude = 31.5,
            longitude = 34.5
        )
    }

    private fun createPackage(
        id: String,
        origin: Warehouse,
        destination: Warehouse
    ): Package {
        return Package(
            id = id,
            weightByKg = 10.0,
            priority = "STANDARD",
            originWarehouse = origin,
            destinationWarehouse = destination
        )
    }

    private fun createRoute(
        id: String,
        origin: Warehouse,
        destination: Warehouse,
        distanceKm: Double,
        delayMin: Int
    ): Route {
        return Route(
            id = id,
            originWarehouse = origin,
            destinationWarehouse = destination,
            distanceKm = distanceKm,
            typicalDelayMin = delayMin
        )
    }
}
