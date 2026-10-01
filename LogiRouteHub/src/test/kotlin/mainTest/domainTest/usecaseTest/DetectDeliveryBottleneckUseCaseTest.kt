package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth
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
import org.junit.jupiter.api.Test

class DetectDeliveryBottleneckUseCaseTest {

    @Test
    fun `returns potential bottlenecks when cargo weight exceeds vehicle capacity`() = runTest {
        // Given
        val warehouseRepository = mockk<WarehouseRepository>()

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

        coEvery {
            warehouseRepository.findAll()
        } returns Result.success(listOf(overloadedWarehouse, normalWarehouse))

        val useCase = DetectPotentialDeliveryBottleneckUseCase(warehouseRepository)


        val result = useCase()


        Truth.assertThat(result.isSuccess).isTrue()

        val bottlenecks = result.getOrNull()
        Truth.assertThat(bottlenecks).isNotNull()
        Truth.assertThat(bottlenecks).hasSize(1)

        val bottleneck = bottlenecks!![0]
        Truth.assertThat(bottleneck.warehouse.id).isEqualTo("WH-001")
        Truth.assertThat(bottleneck.totalCargoWeightKg).isEqualTo(1000.0)
        Truth.assertThat(bottleneck.totalVehicleCapacityKg).isEqualTo(600.0)
        Truth.assertThat(bottleneck.capacityGapKg).isEqualTo(400.0)

        coVerify(exactly = 1) { warehouseRepository.findAll() }
    }

    @Test
    fun `returns empty list when all warehouses have sufficient vehicle capacity`() = runTest {

        val warehouseRepository = mockk<WarehouseRepository>()

        val normalWarehouse = createWarehouse(
            id = "WH-001",
            cargoList = listOf(createPackage(weight = 300.0)),
            vehicleList = listOf(createVehicle(capacity = 500.0))
        )

        coEvery {
            warehouseRepository.findAll()
        } returns Result.success(listOf(normalWarehouse))

        val useCase = DetectPotentialDeliveryBottleneckUseCase(warehouseRepository)


        val result = useCase()


        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.getOrNull()).isEmpty()

        coVerify(exactly = 1) { warehouseRepository.findAll() }
    }

    @Test
    fun `returns failure when warehouse repository fails`() = runTest {

        val warehouseRepository = mockk<WarehouseRepository>()
        val exception = RuntimeException("Database error")

        coEvery {
            warehouseRepository.findAll()
        } returns Result.failure(exception)

        val useCase = DetectPotentialDeliveryBottleneckUseCase(warehouseRepository)


        val result = useCase()


        Truth.assertThat(result.isFailure).isTrue()
        Truth.assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) { warehouseRepository.findAll() }
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