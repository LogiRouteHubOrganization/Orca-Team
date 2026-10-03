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
    fun `should dispatch packages within available vehicle capacity`() = runTest {
        // Given
        val vehicle = mockVehicle(capacity = 100.0)
        val package1 = mockPackage(weight = 30.0, priority = "URGENT")
        val package2 = mockPackage(weight = 40.0, priority = "STANDARD")
        val warehouse = mockWarehouse(package1, package2)

        stubRepositories(vehicle, warehouse)

        // When
        val result = dispatchVehicleUseCase(
            DispatchVehicleRequest(
                vehicleId = "TRK-000001",
                warehouseId = "WH-000001"
            )
        )

        // Then
        assertThat(result).isEqualTo(
            Result.success(listOf(package1, package2))
        )
    }

    @Test
    fun `should consider already loaded vehicle weight when selecting packages`() = runTest {
        // Given
        val vehicle = mockVehicle(
            capacity = 100.0,
            loadedWeight = 60.0
        )
        val package1 = mockPackage(weight = 30.0, priority = "STANDARD")
        val package2 = mockPackage(weight = 20.0, priority = "LOW")
        val warehouse = mockWarehouse(package1, package2)

        stubRepositories(vehicle, warehouse)

        // When
        val result = dispatchVehicleUseCase(
            DispatchVehicleRequest(
                vehicleId = "TRK-000001",
                warehouseId = "WH-000001"
            )
        )

        // Then
        assertThat(result).isEqualTo(
            Result.success(listOf(package1))
        )
    }

    @Test
    fun `should skip packages that exceed remaining capacity`() = runTest {
        // Given
        val vehicle = mockVehicle(capacity = 50.0)
        val heavyPackage = mockPackage(
            weight = 60.0,
            priority = "STANDARD"
        )
        val warehouse = mockWarehouse(heavyPackage)

        stubRepositories(vehicle, warehouse)

        // When
        val result = dispatchVehicleUseCase(
            DispatchVehicleRequest(
                vehicleId = "TRK-000001",
                warehouseId = "WH-000001"
            )
        )

        // Then
        assertThat(result).isEqualTo(
            Result.success(emptyList<Package>())
        )
        verify(exactly = 0) {
            vehicle.addPackage(any())
        }

        verify(exactly = 0) {
            warehouse.removePackage(any())
        }
    }

    @Test
    fun `should add dispatched packages to vehicle and remove them from warehouse`() = runTest {
        // Given
        val vehicle = mockVehicle()
        val logisticsPackage = mockPackage(
            weight = 40.0,
            priority = "STANDARD"
        )
        val warehouse = mockWarehouse(logisticsPackage)

        stubRepositories(vehicle, warehouse)

        // When
        dispatchVehicleUseCase(
            DispatchVehicleRequest(
                vehicleId = "TRK-000001",
                warehouseId = "WH-000001"
            )
        )

        // Then
        verify(exactly = 1) {
            vehicle.addPackage(logisticsPackage)
        }

        verify(exactly = 1) {
            warehouse.removePackage(logisticsPackage)
        }
    }

    @Test
    fun `should dispatch urgent package using express dispatch flow`() = runTest {
        // Given
        val vehicle = mockVehicle()
        val urgentPackage = mockPackage(
            weight = 30.0,
            priority = "URGENT"
        )
        val warehouse = mockWarehouse(urgentPackage)

        stubRepositories(vehicle, warehouse)

        // When
        val result = dispatchVehicleUseCase(
            DispatchVehicleRequest(
                vehicleId = "TRK-000001",
                warehouseId = "WH-000001"
            )
        )

        // Then
        assertThat(result).isEqualTo(
            Result.success(listOf(urgentPackage))
        )

        verify(exactly = 1) {
            vehicle.addPackage(urgentPackage)
        }
    }

    @Test
    fun `should dispatch standard package using standard dispatch flow`() = runTest {
        // Given
        val vehicle = mockVehicle()
        val standardPackage = mockPackage(
            weight = 30.0,
            priority = "STANDARD"
        )
        val warehouse = mockWarehouse(standardPackage)

        stubRepositories(vehicle, warehouse)

        // When
        val result = dispatchVehicleUseCase(
            DispatchVehicleRequest(
                vehicleId = "TRK-000001",
                warehouseId = "WH-000001"
            )
        )

        // Then
        assertThat(result).isEqualTo(
            Result.success(listOf(standardPackage))
        )

        verify(exactly = 1) {
            vehicle.addPackage(standardPackage)
        }
    }

    @Test
    fun `should dispatch low priority package using standard dispatch flow`() = runTest {
        // Given
        val vehicle = mockVehicle()
        val lowPriorityPackage = mockPackage(
            weight = 30.0,
            priority = "LOW"
        )
        val warehouse = mockWarehouse(lowPriorityPackage)

        stubRepositories(vehicle, warehouse)

        // When
        val result = dispatchVehicleUseCase(
            DispatchVehicleRequest(
                vehicleId = "TRK-000001",
                warehouseId = "WH-000001"
            )
        )

        // Then
        assertThat(result).isEqualTo(
            Result.success(listOf(lowPriorityPackage))
        )

        verify(exactly = 1) {
            vehicle.addPackage(lowPriorityPackage)
        }
    }

    @Test
    fun `should return failure for unsupported package priority`() = runTest {
        // Given
        val vehicle = mockVehicle()
        val invalidPackage = mockPackage(
            weight = 30.0,
            priority = "INVALID"
        )
        val warehouse = mockWarehouse(invalidPackage)

        stubRepositories(vehicle, warehouse)

        // When
        val result = dispatchVehicleUseCase(
            DispatchVehicleRequest(
                vehicleId = "TRK-000001",
                warehouseId = "WH-000001"
            )
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `should return empty list when vehicle does not exist`() = runTest {
        // Given
        coEvery {
            vehicleRepository.findById("TRK-000001")
        } returns Result.success(null)

        // When
        val result = dispatchVehicleUseCase(
            DispatchVehicleRequest(
                vehicleId = "TRK-000001",
                warehouseId = "WH-000001"
            )
        )

        // Then
        assertThat(result).isEqualTo(
            Result.success(emptyList<Package>())
        )

        coVerify(exactly = 0) {
            warehouseRepository.findById(any())
        }
    }

    @Test
    fun `should return empty list when warehouse does not exist`() = runTest {
        // Given
        val vehicle = mockVehicle()

        coEvery {
            vehicleRepository.findById("TRK-000001")
        } returns Result.success(vehicle)

        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(null)

        // When
        val result = dispatchVehicleUseCase(
            DispatchVehicleRequest(
                vehicleId = "TRK-000001",
                warehouseId = "WH-000001"
            )
        )

        // Then
        assertThat(result).isEqualTo(
            Result.success(emptyList<Package>())
        )
    }

    @Test
    fun `should return empty list when warehouse cargo queue is empty`() = runTest {
        // Given
        val vehicle = mockVehicle()
        val warehouse = mockk<Warehouse>(relaxed = true)

        every {
            warehouse.cargoQueue
        } returns emptyList()

        stubRepositories(vehicle, warehouse)

        // When
        val result = dispatchVehicleUseCase(
            DispatchVehicleRequest(
                vehicleId = "TRK-000001",
                warehouseId = "WH-000001"
            )
        )

        // Then
        assertThat(result).isEqualTo(
            Result.success(emptyList<Package>())
        )
    }
    private fun mockVehicle(
        id: String = "TRK-000001",
        capacity: Double = 100.0,
        loadedWeight: Double = 0.0
    ): Vehicle {
        val vehicle = mockk<Vehicle>(relaxed = true)

        every {
            vehicle.id
        } returns id

        every {
            vehicle.maxCapacityKg
        } returns capacity

        every {
            vehicle.calculateTotalLoadedWeightKg()
        } returns loadedWeight

        every {
            vehicle.addPackage(any())
        } just Runs

        return vehicle
    }

    private fun mockPackage(
        id: String = "PKG-000001",
        weight: Double,
        priority: String
    ): Package {
        val logisticsPackage = mockk<Package>()

        every {
            logisticsPackage.id
        } returns id

        every {
            logisticsPackage.weightByKg
        } returns weight

        every {
            logisticsPackage.priority
        } returns priority

        every {
            logisticsPackage.state
        } returns CreatedState()

        every {
            logisticsPackage.state = any()
        } just Runs

        return logisticsPackage
    }

    private fun mockWarehouse(
        vararg packages: Package
    ): Warehouse {
        val warehouse = mockk<Warehouse>(relaxed = true)

        every {
            warehouse.cargoQueue
        } returns packages.toList()

        every {
            warehouse.removePackage(any())
        } just Runs

        return warehouse
    }

    private fun stubRepositories(
        vehicle: Vehicle,
        warehouse: Warehouse
    ) {
        coEvery {
            vehicleRepository.findById("TRK-000001")
        } returns Result.success(vehicle)

        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(warehouse)
    }
}

