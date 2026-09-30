package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Vehicle
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.usecase.FindStationedVehiclesByCapacityUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FindStationedVehiclesByCapacityUseCaseTest {

    private val warehouseRepository = mockk<WarehouseRepository>()

    private lateinit var useCase: FindStationedVehiclesByCapacityUseCase

    @BeforeEach
    fun setUp() {
        useCase = FindStationedVehiclesByCapacityUseCase(
            warehouseRepository = warehouseRepository
        )
    }


    @Test
    fun `should return vehicles that have enough available capacity`() = runTest {
        // Given
        val warehouseId = "WH-000003"
        val requiredLoadKg = 30.0

        val vehicle1 = mockk<Vehicle>()
        val vehicle2 = mockk<Vehicle>()

        every { vehicle1.maxCapacityKg } returns 100.0
        every { vehicle1.calculateTotalLoadedWeightKg() } returns 40.0

        every { vehicle2.maxCapacityKg } returns 80.0
        every { vehicle2.calculateTotalLoadedWeightKg() } returns 20.0

        val warehouse = mockk<Warehouse>()
        every { warehouse.stationedVehicles } returns listOf(vehicle1, vehicle2)

        coEvery { warehouseRepository.findById(warehouseId) } returns Result.success(warehouse)

        // When
        val result = useCase(
            warehouseId = warehouseId,
            requiredLoadKg = requiredLoadKg
        )

        // Then
        assertThat(result).isEqualTo(Result.success(listOf(vehicle1, vehicle2)))
    }

    @Test
    fun `should include vehicle when available capacity equals required load`() = runTest {
        // Given
        val warehouseId = "WH-000001"
        val requiredLoadKg = 40.0

        val vehicle = mockk<Vehicle>()
        every { vehicle.maxCapacityKg } returns 100.0
        every { vehicle.calculateTotalLoadedWeightKg() } returns 60.0

        val warehouse = mockk<Warehouse>()
        every { warehouse.stationedVehicles } returns listOf(vehicle)

        coEvery { warehouseRepository.findById(warehouseId) } returns Result.success(warehouse)

        // When
        val result = useCase(
            warehouseId = warehouseId,
            requiredLoadKg = requiredLoadKg
        )

        // Then
        assertThat(result).isEqualTo(Result.success(listOf(vehicle)))
    }

    @Test
    fun `should return only vehicles that have enough available capacity`() = runTest {
        // Given
        val warehouseId = "WH-000002"
        val requiredLoadKg = 50.0

        val suitableVehicle = mockk<Vehicle>()
        val unsuitableVehicle = mockk<Vehicle>()

        every { suitableVehicle.maxCapacityKg } returns 100.0
        every { suitableVehicle.calculateTotalLoadedWeightKg() } returns 40.0

        every { unsuitableVehicle.maxCapacityKg } returns 100.0
        every { unsuitableVehicle.calculateTotalLoadedWeightKg() } returns 60.0

        val warehouse = mockk<Warehouse>()
        every { warehouse.stationedVehicles } returns listOf(suitableVehicle, unsuitableVehicle)

        coEvery { warehouseRepository.findById(warehouseId) } returns Result.success(warehouse)

        // When
        val result = useCase(
            warehouseId = warehouseId,
            requiredLoadKg = requiredLoadKg
        )

        // Then
        assertThat(result).isEqualTo(Result.success(listOf(suitableVehicle)))
    }


    @Test
    fun `should return empty list when no vehicle has enough available capacity`() = runTest {
        // Given
        val warehouseId = "WH-000004"
        val requiredLoadKg = 70.0

        val vehicle1 = mockk<Vehicle>()
        val vehicle2 = mockk<Vehicle>()

        every { vehicle1.maxCapacityKg } returns 100.0
        every { vehicle1.calculateTotalLoadedWeightKg() } returns 50.0

        every { vehicle2.maxCapacityKg } returns 80.0
        every { vehicle2.calculateTotalLoadedWeightKg() } returns 30.0

        val warehouse = mockk<Warehouse>()
        every { warehouse.stationedVehicles } returns listOf(vehicle1, vehicle2)

        coEvery { warehouseRepository.findById(warehouseId) } returns Result.success(warehouse)

        // When
        val result = useCase(
            warehouseId = warehouseId,
            requiredLoadKg = requiredLoadKg
        )

        // Then
        assertThat(result).isEqualTo(Result.success(emptyList<Vehicle>()))
    }

    @Test
    fun `should exclude vehicle when available capacity is less than required load`() = runTest {
        // Given
        val warehouseId = "WH-000001"
        val requiredLoadKg = 40.0

        val vehicle = mockk<Vehicle>()
        every { vehicle.maxCapacityKg } returns 100.0
        every { vehicle.calculateTotalLoadedWeightKg() } returns 61.0

        val warehouse = mockk<Warehouse>()
        every { warehouse.stationedVehicles } returns listOf(vehicle)

        coEvery { warehouseRepository.findById(warehouseId) } returns Result.success(warehouse)

        // When
        val result = useCase(
            warehouseId = warehouseId,
            requiredLoadKg = requiredLoadKg
        )

        // Then
        assertThat(result).isEqualTo(Result.success(emptyList<Vehicle>()))
    }

    @Test
    fun `should return empty list when warehouse has no stationed vehicles`() = runTest {
        // Given
        val warehouseId = "WH-000001"
        val requiredLoadKg = 50.0

        val warehouse = mockk<Warehouse>()
        every { warehouse.stationedVehicles } returns emptyList()

        coEvery { warehouseRepository.findById(warehouseId) } returns Result.success(warehouse)

        // When
        val result = useCase(
            warehouseId = warehouseId,
            requiredLoadKg = requiredLoadKg
        )

        // Then
        assertThat(result).isEqualTo(Result.success(emptyList<Vehicle>()))
    }


    @Test
    fun `should return empty list when warehouse does not exist`() = runTest {
        // Given
        val warehouseId = "WH-000001"
        val requiredLoadKg = 30.0

        coEvery { warehouseRepository.findById(warehouseId) } returns Result.success(null)

        // When
        val result = useCase(
            warehouseId = warehouseId,
            requiredLoadKg = requiredLoadKg
        )

        // Then
        assertThat(result).isEqualTo(Result.success(emptyList<Vehicle>()))
    }

    @Test
    fun `should return failure when warehouse repository fails`() = runTest {
        // Given
        val warehouseId = "WH-000001"
        val requiredLoadKg = 50.0
        val exception = RuntimeException("Database error")

        coEvery { warehouseRepository.findById(warehouseId) } returns Result.failure(exception)

        // When
        val result = useCase(
            warehouseId = warehouseId,
            requiredLoadKg = requiredLoadKg
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isSameInstanceAs(exception)
    }
}
