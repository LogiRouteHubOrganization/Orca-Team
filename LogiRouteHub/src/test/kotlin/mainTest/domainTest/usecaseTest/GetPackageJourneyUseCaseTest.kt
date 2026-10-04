package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Package
import domain.model.Route
import domain.model.Warehouse
import domain.repository.PackageRepository
import domain.usecase.FindOptimalPathUseCase
import domain.usecase.GetPackageJourneyUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetPackageJourneyUseCaseTest {

    private val packageRepository = mockk<PackageRepository>()
    private val findOptimalPathUseCase = mockk<FindOptimalPathUseCase>()

    private val useCase = GetPackageJourneyUseCase(
        packageRepository = packageRepository,
        findOptimalPathUseCase = findOptimalPathUseCase
    )

    @Test
    fun `returns package journey when package and path exist`() = runTest {
        // Given
        val data = createJourneyData()

        stubSuccessfulJourney(data)

        // When
        val result = useCase(data.logisticsPackage.id)

        // Then
        assertThat(result.getOrNull()).isNotNull()
    }

    @Test
    fun `uses package origin and destination to find optimal path`() = runTest {
        // Given
        val data = createJourneyData()

        stubSuccessfulJourney(data)

        // When
        useCase(data.logisticsPackage.id)

        // Then
        coVerify(exactly = 1) {
            findOptimalPathUseCase(
                startWarehouseId = data.warehouseA.id,
                destinationWarehouseId = data.warehouseC.id
            )
        }
    }

    @Test
    fun `returns warehouses from optimal path in journey`() = runTest {
        // Given
        val data = createJourneyData()

        stubSuccessfulJourney(data)

        // When
        val result = useCase(data.logisticsPackage.id)

        // Then
        assertThat(result.getOrThrow()!!.warehouses)
            .containsExactly(
                data.warehouseA,
                data.warehouseB,
                data.warehouseC
            )
            .inOrder()
    }

    @Test
    fun `returns routes connecting warehouses in journey`() = runTest {
        // Given
        val data = createJourneyData()

        stubSuccessfulJourney(data)

        // When
        val result = useCase(data.logisticsPackage.id)

        // Then
        assertThat(result.getOrThrow()!!.routes)
            .containsExactly(
                data.routeAB,
                data.routeBC
            )
            .inOrder()
    }

    @Test
    fun `calculates total distance of journey routes`() = runTest {
        // Given
        val data = createJourneyData()

        stubSuccessfulJourney(data)

        // When
        val result = useCase(data.logisticsPackage.id)

        // Then
        assertThat(result.getOrThrow()!!.totalDistanceKm)
            .isEqualTo(30.0)
    }

    @Test
    fun `calculates total delay of journey routes`() = runTest {
        // Given
        val data = createJourneyData()

        stubSuccessfulJourney(data)

        // When
        val result = useCase(data.logisticsPackage.id)

        // Then
        assertThat(result.getOrThrow()!!.totalTypicalDelayMin)
            .isEqualTo(15)
    }

    @Test
    fun `returns null when package does not exist`() = runTest {
        // Given
        coEvery {
            packageRepository.findById("PKG-000001")
        } returns Result.success(null)

        // When
        val result = useCase("PKG-000001")

        // Then
        assertThat(result.getOrNull()).isNull()
    }

    @Test
    fun `does not find path when package does not exist`() = runTest {
        // Given
        coEvery {
            packageRepository.findById("PKG-000001")
        } returns Result.success(null)

        // When
        useCase("PKG-000001")

        // Then
        coVerify(exactly = 0) {
            findOptimalPathUseCase(any(), any())
        }
    }

    @Test
    fun `returns failure when package repository fails`() = runTest {
        // Given
        val exception = RuntimeException(
            "Package repository failed"
        )

        coEvery {
            packageRepository.findById("PKG-000001")
        } returns Result.failure(exception)

        // When
        val result = useCase("PKG-000001")

        // Then
        assertThat(result.exceptionOrNull())
            .isSameInstanceAs(exception)
    }

    @Test
    fun `returns failure when optimal path calculation fails`() = runTest {
        // Given
        val data = createJourneyData()

        val exception = RuntimeException(
            "Optimal path calculation failed"
        )

        coEvery {
            packageRepository.findById(
                data.logisticsPackage.id
            )
        } returns Result.success(data.logisticsPackage)

        coEvery {
            findOptimalPathUseCase(
                startWarehouseId = data.warehouseA.id,
                destinationWarehouseId = data.warehouseC.id
            )
        } returns Result.failure(exception)

        // When
        val result = useCase(data.logisticsPackage.id)

        // Then
        assertThat(result.exceptionOrNull())
            .isSameInstanceAs(exception)
    }

    private fun stubSuccessfulJourney(
        data: JourneyTestData
    ) {
        coEvery {
            packageRepository.findById(
                data.logisticsPackage.id
            )
        } returns Result.success(data.logisticsPackage)

        coEvery {
            findOptimalPathUseCase(
                startWarehouseId = data.warehouseA.id,
                destinationWarehouseId = data.warehouseC.id
            )
        } returns Result.success(
            listOf(
                data.warehouseA,
                data.warehouseB,
                data.warehouseC
            )
        )
    }

    private fun createJourneyData(): JourneyTestData {

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

        return JourneyTestData(
            warehouseA = warehouseA,
            warehouseB = warehouseB,
            warehouseC = warehouseC,
            routeAB = routeAB,
            routeBC = routeBC,
            logisticsPackage = logisticsPackage
        )
    }

    private fun createWarehouse(
        id: String
    ): Warehouse {
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

    private data class JourneyTestData(
        val warehouseA: Warehouse,
        val warehouseB: Warehouse,
        val warehouseC: Warehouse,
        val routeAB: Route,
        val routeBC: Route,
        val logisticsPackage: Package
    )
}
