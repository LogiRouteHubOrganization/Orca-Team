package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Package
import domain.model.Vehicle
import domain.model.Warehouse
import domain.model.request.DispatchVehicleRequest
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository
import domain.usecase.DispatchVehicleUseCase
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

    private lateinit var dispatchVehicleUseCase: DispatchVehicleUseCase

    @BeforeEach
    fun setUp() {
        dispatchVehicleUseCase = DispatchVehicleUseCase(
            vehicleRepository = vehicleRepository,
            warehouseRepository = warehouseRepository
        )
    }

    @Test
    fun `should dispatch packages when vehicle and warehouse exist`() = runTest {
        // Given
        val vehicle = mockk<Vehicle>(relaxed = true)
        val warehouse = mockk<Warehouse>(relaxed = true)
        val package1 = mockk<Package>()
        val package2 = mockk<Package>()

        every { vehicle.maxCapacityKg } returns 100.0
        every { vehicle.calculateTotalLoadedWeightKg() } returns 0.0
        every { package1.weightByKg } returns 30.0
        every { package2.weightByKg } returns 40.0
        every { warehouse.cargoQueue } returns listOf(package1, package2)

        coEvery { vehicleRepository.findById("TRK-000001") } returns Result.success(vehicle)
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.success(warehouse)

        every { vehicle.addPackage(any()) } just Runs
        every { warehouse.removePackage(any()) } just Runs

        val input = DispatchVehicleRequest(
            vehicleId = "TRK-000001",
            warehouseId = "WH-000001"
        )

        // When
        val result = dispatchVehicleUseCase(input)

        // Then
        assertThat(result).isEqualTo(Result.success(listOf(package1, package2)))
    }

    @Test
    fun `should calculate available capacity after existing vehicle load`() = runTest {
        // Given
        val vehicleId = "TRK-000008"
        val warehouseId = "WH-000004"
        val vehicle = mockk<Vehicle>(relaxed = true)
        val warehouse = mockk<Warehouse>(relaxed = true)
        val package1 = mockk<Package>()
        val package2 = mockk<Package>()

        every { vehicle.maxCapacityKg } returns 100.0
        every { vehicle.calculateTotalLoadedWeightKg() } returns 60.0
        every { package1.weightByKg } returns 30.0
        every { package2.weightByKg } returns 20.0
        every { warehouse.cargoQueue } returns listOf(package1, package2)

        coEvery { vehicleRepository.findById(vehicleId) } returns Result.success(vehicle)
        coEvery { warehouseRepository.findById(warehouseId) } returns Result.success(warehouse)

        every { vehicle.addPackage(any()) } just Runs
        every { warehouse.removePackage(any()) } just Runs

        val input = DispatchVehicleRequest(
            vehicleId = vehicleId,
            warehouseId = warehouseId
        )

        // When
        val result = dispatchVehicleUseCase(input)

        // Then
        assertThat(result).isEqualTo(Result.success(listOf(package1)))
    }

    @Test
    fun `should dispatch only packages that fit within remaining capacity`() = runTest {
        // Given
        val vehicleId = "TRK-000003"
        val warehouseId = "WH-000002"
        val vehicle = mockk<Vehicle>(relaxed = true)
        val warehouse = mockk<Warehouse>(relaxed = true)
        val package1 = mockk<Package>()
        val package2 = mockk<Package>()
        val package3 = mockk<Package>()

        every { vehicle.maxCapacityKg } returns 100.0
        every { vehicle.calculateTotalLoadedWeightKg() } returns 0.0
        every { package1.weightByKg } returns 40.0
        every { package2.weightByKg } returns 30.0
        every { package3.weightByKg } returns 50.0
        every { warehouse.cargoQueue } returns listOf(package1, package2, package3)

        coEvery { vehicleRepository.findById(vehicleId) } returns Result.success(vehicle)
        coEvery { warehouseRepository.findById(warehouseId) } returns Result.success(warehouse)

        every { vehicle.addPackage(any()) } just Runs
        every { warehouse.removePackage(any()) } just Runs

        val input = DispatchVehicleRequest(
            vehicleId = vehicleId,
            warehouseId = warehouseId
        )

        // When
        val result = dispatchVehicleUseCase(input)

        // Then
        assertThat(result).isEqualTo(Result.success(listOf(package1, package2)))
    }

    @Test
    fun `should add dispatched packages to vehicle and remove them from warehouse`() = runTest {
        // Given
        val vehicleId = "TRK-000001"
        val warehouseId = "WH-000004"
        val vehicle = mockk<Vehicle>(relaxed = true)
        val warehouse = mockk<Warehouse>(relaxed = true)
        val package1 = mockk<Package>()

        every { vehicle.maxCapacityKg } returns 100.0
        every { vehicle.calculateTotalLoadedWeightKg() } returns 0.0
        every { package1.weightByKg } returns 40.0
        every { warehouse.cargoQueue } returns listOf(package1)

        coEvery { vehicleRepository.findById(vehicleId) } returns Result.success(vehicle)
        coEvery { warehouseRepository.findById(warehouseId) } returns Result.success(warehouse)

        every { vehicle.addPackage(package1) } just Runs
        every { warehouse.removePackage(package1) } just Runs

        val input = DispatchVehicleRequest(
            vehicleId = vehicleId,
            warehouseId = warehouseId
        )

        // When
        dispatchVehicleUseCase(input)

        // Then
        verify(exactly = 1) { vehicle.addPackage(package1) }
        verify(exactly = 1) { warehouse.removePackage(package1) }
    }


    @Test
    fun `should return empty list when vehicle does not exist`() = runTest {
        // Given
        val vehicleId = "TRK-000001"
        val warehouseId = "WH-000001"

        coEvery { vehicleRepository.findById(vehicleId) } returns Result.success(null)

        val input = DispatchVehicleRequest(
            vehicleId = vehicleId,
            warehouseId = warehouseId
        )

        // When
        val result = dispatchVehicleUseCase(input)

        // Then
        assertThat(result).isEqualTo(Result.success(emptyList<Package>()))

        coVerify(exactly = 0) {
            warehouseRepository.findById(any())
        }
    }

    @Test
    fun `should return empty list when warehouse does not exist`() = runTest {
        // Given
        val vehicleId = "TRK-000001"
        val warehouseId = "WH-000001"
        val vehicle = mockk<Vehicle>(relaxed = true)

        coEvery { vehicleRepository.findById(vehicleId) } returns Result.success(vehicle)
        coEvery { warehouseRepository.findById(warehouseId) } returns Result.success(null)

        val input = DispatchVehicleRequest(
            vehicleId = vehicleId,
            warehouseId = warehouseId
        )

        // When
        val result = dispatchVehicleUseCase(input)

        // Then
        assertThat(result).isEqualTo(Result.success(emptyList<Package>()))
    }

    @Test
    fun `should not dispatch package when its weight exceeds remaining capacity`() = runTest {
        // Given
        val vehicleId = "TRK-000001"
        val warehouseId = "WH-000001"
        val vehicle = mockk<Vehicle>(relaxed = true)
        val warehouse = mockk<Warehouse>(relaxed = true)
        val heavyPackage = mockk<Package>()

        every { vehicle.maxCapacityKg } returns 50.0
        every { vehicle.calculateTotalLoadedWeightKg() } returns 0.0
        every { heavyPackage.weightByKg } returns 60.0
        every { warehouse.cargoQueue } returns listOf(heavyPackage)

        coEvery { vehicleRepository.findById(vehicleId) } returns Result.success(vehicle)
        coEvery { warehouseRepository.findById(warehouseId) } returns Result.success(warehouse)

        val input = DispatchVehicleRequest(
            vehicleId = vehicleId,
            warehouseId = warehouseId
        )

        // When
        val result = dispatchVehicleUseCase(input)

        // Then
        assertThat(result).isEqualTo(Result.success(emptyList<Package>()))

        verify(exactly = 0) { vehicle.addPackage(any()) }
        verify(exactly = 0) { warehouse.removePackage(any()) }
    }

    @Test
    fun `should return empty list when vehicle has no available capacity`() = runTest {
        // Given
        val vehicleId = "TRK-000001"
        val warehouseId = "WH-000001"
        val vehicle = mockk<Vehicle>(relaxed = true)
        val warehouse = mockk<Warehouse>(relaxed = true)
        val package1 = mockk<Package>()

        every { vehicle.maxCapacityKg } returns 100.0
        every { vehicle.calculateTotalLoadedWeightKg() } returns 100.0
        every { package1.weightByKg } returns 20.0
        every { warehouse.cargoQueue } returns listOf(package1)

        coEvery { vehicleRepository.findById(vehicleId) } returns Result.success(vehicle)
        coEvery { warehouseRepository.findById(warehouseId) } returns Result.success(warehouse)

        val input = DispatchVehicleRequest(
            vehicleId = vehicleId,
            warehouseId = warehouseId
        )

        // When
        val result = dispatchVehicleUseCase(input)

        // Then
        assertThat(result).isEqualTo(Result.success(emptyList<Package>()))

        verify(exactly = 0) { vehicle.addPackage(any()) }
        verify(exactly = 0) { warehouse.removePackage(any()) }
    }

    @Test
    fun `should return empty list when warehouse cargo queue is empty`() = runTest {
        // Given
        val vehicleId = "TRK-000002"
        val warehouseId = "WH-000002"
        val vehicle = mockk<Vehicle>(relaxed = true)
        val warehouse = mockk<Warehouse>(relaxed = true)

        every { vehicle.maxCapacityKg } returns 100.0
        every { vehicle.calculateTotalLoadedWeightKg() } returns 0.0
        every { warehouse.cargoQueue } returns emptyList()

        coEvery { vehicleRepository.findById(vehicleId) } returns Result.success(vehicle)
        coEvery { warehouseRepository.findById(warehouseId) } returns Result.success(warehouse)

        val input = DispatchVehicleRequest(
            vehicleId = vehicleId,
            warehouseId = warehouseId
        )

        // When
        val result = dispatchVehicleUseCase(input)

        // Then
        assertThat(result).isEqualTo(Result.success(emptyList<Package>()))
    }
}
