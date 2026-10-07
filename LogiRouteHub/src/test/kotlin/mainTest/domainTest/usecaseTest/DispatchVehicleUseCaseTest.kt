package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Package
import domain.model.Vehicle
import domain.model.Warehouse
import domain.model.request.DispatchVehicleRequest
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository
import domain.state.CreatedState
import domain.usecase.DispatchVehicleUseCase
import domain.usecase.OptimizeCargoByWeightUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DispatchVehicleUseCaseTest {

    private val vehicleRepository = mockk<VehicleRepository>()
    private val warehouseRepository = mockk<WarehouseRepository>()

    private val optimizeCargoByWeightUseCase = OptimizeCargoByWeightUseCase()

    private lateinit var dispatchVehicleUseCase: DispatchVehicleUseCase

    @BeforeEach
    fun setUp() {
        dispatchVehicleUseCase = DispatchVehicleUseCase(
            vehicleRepository = vehicleRepository,
            warehouseRepository = warehouseRepository,
            optimizeCargoByWeightUseCase = optimizeCargoByWeightUseCase
        )
    }

    @Test
    fun `should dispatch packages within available vehicle capacity`() = runTest {
        val vehicle = mockVehicle(capacity = 100.0)
        val package1 = mockPackage(weight = 30.0, priority = "URGENT")
        val package2 = mockPackage(weight = 40.0, priority = "STANDARD")
        val warehouse = mockWarehouse(package1, package2)
        stubRepositories(vehicle, warehouse)

        val result = dispatchVehicleUseCase(createDispatchRequest())

        assertThat(result).isEqualTo(Result.success(listOf(package1, package2)))
    }

    @Test
    fun `should consider already loaded vehicle weight when selecting packages`() = runTest {
        val vehicle = mockVehicle(capacity = 100.0, loadedWeight = 60.0)
        val package1 = mockPackage(weight = 30.0, priority = "STANDARD")
        val package2 = mockPackage(weight = 20.0, priority = "LOW")
        val warehouse = mockWarehouse(package1, package2)
        stubRepositories(vehicle, warehouse)

        val result = dispatchVehicleUseCase(createDispatchRequest())

        assertThat(result).isEqualTo(Result.success(listOf(package1)))
    }

    @Test
    fun `should return empty list when packages exceed remaining capacity`() = runTest {
        val vehicle = mockVehicle(capacity = 50.0)
        val heavyPackage = mockPackage(weight = 60.0, priority = "STANDARD")
        val warehouse = mockWarehouse(heavyPackage)
        stubRepositories(vehicle, warehouse)

        val result = dispatchVehicleUseCase(createDispatchRequest())

        assertThat(result).isEqualTo(Result.success(emptyList<Package>()))
    }

    @Test
    fun `should not add packages to vehicle when remaining capacity is exceeded`() = runTest {
        val vehicle = mockVehicle(capacity = 50.0)
        val heavyPackage = mockPackage(weight = 60.0, priority = "STANDARD")
        val warehouse = mockWarehouse(heavyPackage)
        stubRepositories(vehicle, warehouse)

        dispatchVehicleUseCase(createDispatchRequest())

        verify(exactly = 0) { vehicle.addPackage(any()) }
    }

    @Test
    fun `should not remove packages from warehouse when remaining capacity is exceeded`() = runTest {
        val vehicle = mockVehicle(capacity = 50.0)
        val heavyPackage = mockPackage(weight = 60.0, priority = "STANDARD")
        val warehouse = mockWarehouse(heavyPackage)
        stubRepositories(vehicle, warehouse)

        dispatchVehicleUseCase(createDispatchRequest())

        verify(exactly = 0) { warehouse.removePackage(any()) }
    }


    @Test
    fun `should add dispatched package to vehicle`() = runTest {
        val vehicle = mockVehicle()
        val logisticsPackage = mockPackage(weight = 40.0, priority = "STANDARD")
        val warehouse = mockWarehouse(logisticsPackage)
        stubRepositories(vehicle, warehouse)

        dispatchVehicleUseCase(createDispatchRequest())

        verify(exactly = 1) { vehicle.addPackage(logisticsPackage) }
    }

    @Test
    fun `should remove dispatched package from warehouse`() = runTest {
        val vehicle = mockVehicle()
        val logisticsPackage = mockPackage(weight = 40.0, priority = "STANDARD")
        val warehouse = mockWarehouse(logisticsPackage)
        stubRepositories(vehicle, warehouse)

        dispatchVehicleUseCase(createDispatchRequest())

        verify(exactly = 1) { warehouse.removePackage(logisticsPackage) }
    }

    @Test
    fun `should return dispatched urgent package using express dispatch flow`() = runTest {
        val vehicle = mockVehicle()
        val urgentPackage = mockPackage(weight = 30.0, priority = "URGENT")
        val warehouse = mockWarehouse(urgentPackage)
        stubRepositories(vehicle, warehouse)

        val result = dispatchVehicleUseCase(createDispatchRequest())

        assertThat(result).isEqualTo(Result.success(listOf(urgentPackage)))
    }

    @Test
    fun `should add urgent package to vehicle during express dispatch`() = runTest {
        val vehicle = mockVehicle()
        val urgentPackage = mockPackage(weight = 30.0, priority = "URGENT")
        val warehouse = mockWarehouse(urgentPackage)
        stubRepositories(vehicle, warehouse)

        dispatchVehicleUseCase(createDispatchRequest())

        verify(exactly = 1) { vehicle.addPackage(urgentPackage) }
    }

    @Test
    fun `should return dispatched standard package using standard dispatch flow`() = runTest {
        val vehicle = mockVehicle()
        val standardPackage = mockPackage(weight = 30.0, priority = "STANDARD")
        val warehouse = mockWarehouse(standardPackage)
        stubRepositories(vehicle, warehouse)

        val result = dispatchVehicleUseCase(createDispatchRequest())

        assertThat(result).isEqualTo(Result.success(listOf(standardPackage)))
    }

    @Test
    fun `should return dispatched low priority package using standard dispatch flow`() = runTest {
        val vehicle = mockVehicle()
        val lowPriorityPackage = mockPackage(weight = 30.0, priority = "LOW")
        val warehouse = mockWarehouse(lowPriorityPackage)
        stubRepositories(vehicle, warehouse)

        val result = dispatchVehicleUseCase(createDispatchRequest())

        assertThat(result).isEqualTo(Result.success(listOf(lowPriorityPackage)))
    }


    @Test
    fun `should return failure for unsupported package priority`() = runTest {
        val vehicle = mockVehicle()
        val invalidPackage = mockPackage(weight = 30.0, priority = "INVALID")
        val warehouse = mockWarehouse(invalidPackage)
        stubRepositories(vehicle, warehouse)

        val result = dispatchVehicleUseCase(createDispatchRequest())

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `should throw IllegalArgumentException for unsupported package priority`() = runTest {
        val vehicle = mockVehicle()
        val invalidPackage = mockPackage(weight = 30.0, priority = "INVALID")
        val warehouse = mockWarehouse(invalidPackage)
        stubRepositories(vehicle, warehouse)

        val result = dispatchVehicleUseCase(createDispatchRequest())

        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `should return empty list when vehicle does not exist`() = runTest {
        coEvery { vehicleRepository.findById("TRK-000001") } returns Result.success(null)

        val result = dispatchVehicleUseCase(createDispatchRequest())

        assertThat(result).isEqualTo(Result.success(emptyList<Package>()))
    }

    @Test
    fun `should skip warehouse lookup when vehicle does not exist`() = runTest {
        coEvery { vehicleRepository.findById("TRK-000001") } returns Result.success(null)

        dispatchVehicleUseCase(createDispatchRequest())

        coVerify(exactly = 0) { warehouseRepository.findById(any()) }
    }

    @Test
    fun `should return empty list when warehouse does not exist`() = runTest {
        val vehicle = mockVehicle()
        coEvery { vehicleRepository.findById("TRK-000001") } returns Result.success(vehicle)
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.success(null)

        val result = dispatchVehicleUseCase(createDispatchRequest())

        assertThat(result).isEqualTo(Result.success(emptyList<Package>()))
    }

    @Test
    fun `should return empty list when warehouse cargo queue is empty`() = runTest {
        val vehicle = mockVehicle()
        val warehouse = mockk<Warehouse>(relaxed = true)
        every { warehouse.cargoQueue } returns emptyList()
        stubRepositories(vehicle, warehouse)

        val result = dispatchVehicleUseCase(createDispatchRequest())

        assertThat(result).isEqualTo(Result.success(emptyList<Package>()))
    }


    private fun createDispatchRequest(
        vehicleId: String = "TRK-000001",
        warehouseId: String = "WH-000001"
    ): DispatchVehicleRequest {
        return DispatchVehicleRequest(
            vehicleId = vehicleId,
            warehouseId = warehouseId
        )
    }

    private fun mockVehicle(
        id: String = "TRK-000001",
        capacity: Double = 100.0,
        loadedWeight: Double = 0.0
    ): Vehicle {
        val vehicle = mockk<Vehicle>(relaxed = true)
        every { vehicle.id } returns id
        every { vehicle.maxCapacityKg } returns capacity
        every { vehicle.calculateTotalLoadedWeightKg() } returns loadedWeight
        every { vehicle.addPackage(any()) } just Runs
        return vehicle
    }

    private fun mockPackage(
        id: String = "PKG-000001",
        weight: Double,
        priority: String
    ): Package {
        val logisticsPackage = mockk<Package>(relaxed = true)

        every { logisticsPackage.id } returns id
        every { logisticsPackage.weightByKg } returns weight
        every { logisticsPackage.priority } returns priority
        every { logisticsPackage.state } returns CreatedState()

        return logisticsPackage
    }

    private fun mockWarehouse(vararg packages: Package): Warehouse {
        val warehouse = mockk<Warehouse>(relaxed = true)
        every { warehouse.cargoQueue } returns packages.toList()
        every { warehouse.removePackage(any()) } just Runs
        return warehouse
    }

    private fun stubRepositories(vehicle: Vehicle, warehouse: Warehouse) {
        coEvery { vehicleRepository.findById("TRK-000001") } returns Result.success(vehicle)
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.success(warehouse)
    }
}