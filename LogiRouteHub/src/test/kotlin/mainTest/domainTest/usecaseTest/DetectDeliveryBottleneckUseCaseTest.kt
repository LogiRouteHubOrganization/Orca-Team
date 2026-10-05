package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Package
import domain.model.Vehicle
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.usecase.DetectPotentialDeliveryBottleneckUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DetectDeliveryBottleneckUseCaseTest {

    private val warehouseRepository = mockk<WarehouseRepository>()
    private lateinit var useCase: DetectPotentialDeliveryBottleneckUseCase

    @BeforeEach
    fun setUp() {
        useCase = DetectPotentialDeliveryBottleneckUseCase(warehouseRepository)
    }

    @Test
    fun `should return success result when bottleneck detection executes`() = runTest {
        stubRepositoryWithOverloadedWarehouse()

        val result = useCase()

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `should detect correct number of bottleneck warehouses`() = runTest {
        stubRepositoryWithOverloadedWarehouse()

        val result = useCase()

        assertThat(result.getOrNull()).hasSize(1)
    }

    @Test
    fun `should identify correct warehouse ID with bottleneck`() = runTest {
        stubRepositoryWithOverloadedWarehouse()

        val bottleneck = useCase().getOrNull()?.firstOrNull()

        assertThat(bottleneck?.warehouse?.id).isEqualTo("WH-001")
    }

    @Test
    fun `should calculate correct total cargo weight for bottleneck warehouse`() = runTest {
        stubRepositoryWithOverloadedWarehouse()

        val bottleneck = useCase().getOrNull()?.firstOrNull()

        assertThat(bottleneck?.totalCargoWeightKg).isEqualTo(1000.0)
    }

    @Test
    fun `should calculate correct total vehicle capacity for bottleneck warehouse`() = runTest {
        stubRepositoryWithOverloadedWarehouse()

        val bottleneck = useCase().getOrNull()?.firstOrNull()

        assertThat(bottleneck?.totalVehicleCapacityKg).isEqualTo(600.0)
    }

    @Test
    fun `should calculate correct capacity gap for bottleneck warehouse`() = runTest {
        stubRepositoryWithOverloadedWarehouse()

        val bottleneck = useCase().getOrNull()?.firstOrNull()

        assertThat(bottleneck?.capacityGapKg).isEqualTo(400.0)
    }

    @Test
    fun `should query all warehouses from repository when detecting overloaded warehouse`() = runTest {
        stubRepositoryWithOverloadedWarehouse()

        useCase()

        coVerify(exactly = 1) { warehouseRepository.findAll() }
    }

    @Test
    fun `should return success result when all warehouses have sufficient vehicle capacity`() = runTest {
        stubRepositoryWithNormalWarehouse()

        val result = useCase()

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `should return empty list when all warehouses have sufficient vehicle capacity`() = runTest {
        stubRepositoryWithNormalWarehouse()

        val result = useCase()

        assertThat(result.getOrNull()).isEmpty()
    }

    @Test
    fun `should query all warehouses from repository when capacity is sufficient`() = runTest {
        stubRepositoryWithNormalWarehouse()

        useCase()

        coVerify(exactly = 1) { warehouseRepository.findAll() }
    }

    @Test
    fun `should return failure result when warehouse repository fails`() = runTest {
        val exception = RuntimeException("Database error")
        coEvery { warehouseRepository.findAll() } returns Result.failure(exception)

        val result = useCase()

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `should propagate exact exception when warehouse repository fails`() = runTest {
        val exception = RuntimeException("Database error")
        coEvery { warehouseRepository.findAll() } returns Result.failure(exception)

        val result = useCase()

        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `should query all warehouses from repository even when repository fails`() = runTest {
        val exception = RuntimeException("Database error")
        coEvery { warehouseRepository.findAll() } returns Result.failure(exception)

        useCase()

        coVerify(exactly = 1) { warehouseRepository.findAll() }
    }


    private fun stubRepositoryWithOverloadedWarehouse() {
        val overloadedWarehouse = createWarehouse(
            id = "WH-001",
            cargoList = listOf(createPackage(weight = 400.0), createPackage(weight = 600.0)),
            vehicleList = listOf(createVehicle(capacity = 600.0))
        )

        val normalWarehouse = createWarehouse(
            id = "WH-002",
            cargoList = listOf(createPackage(weight = 200.0)),
            vehicleList = listOf(createVehicle(capacity = 500.0))
        )

        coEvery { warehouseRepository.findAll() } returns Result.success(listOf(overloadedWarehouse, normalWarehouse))
    }

    private fun stubRepositoryWithNormalWarehouse() {
        val normalWarehouse = createWarehouse(
            id = "WH-001",
            cargoList = listOf(createPackage(weight = 300.0)),
            vehicleList = listOf(createVehicle(capacity = 500.0))
        )

        coEvery { warehouseRepository.findAll() } returns Result.success(listOf(normalWarehouse))
    }

    private fun createWarehouse(
        id: String,
        cargoList: List<Package>,
        vehicleList: List<Vehicle>
    ): Warehouse {
        val warehouse = mockk<Warehouse>(relaxed = true)
        every { warehouse.id } returns id
        every { warehouse.cargoQueue } returns cargoList
        every { warehouse.stationedVehicles } returns vehicleList
        return warehouse
    }

    private fun createPackage(weight: Double): Package {
        val packageItem = mockk<Package>(relaxed = true)
        every { packageItem.weightByKg } returns weight
        return packageItem
    }

    private fun createVehicle(capacity: Double): Vehicle {
        val vehicle = mockk<Vehicle>(relaxed = true)
        every { vehicle.maxCapacityKg } returns capacity
        return vehicle
    }
}