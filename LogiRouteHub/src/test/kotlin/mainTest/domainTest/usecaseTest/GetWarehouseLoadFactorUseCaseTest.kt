package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Package
import domain.model.Vehicle
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.usecase.GetWarehouseLoadFactorUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetWarehouseLoadFactorUseCaseTest {

    @Test
    fun `should calculate warehouse load factor correctly`() = runTest {

        // Given
        val warehouseRepository = mockk<WarehouseRepository>()
        val warehouse = mockk<Warehouse>()

        val package1 = mockk<Package>()
        val package2 = mockk<Package>()

        val vehicle1 = mockk<Vehicle>()
        val vehicle2 = mockk<Vehicle>()

        every { package1.weightByKg } returns 100.0
        every { package2.weightByKg } returns 50.0

        every { vehicle1.maxCapacityKg } returns 200.0
        every { vehicle2.maxCapacityKg } returns 100.0

        every { warehouse.cargoQueue } returns listOf(
            package1,
            package2
        )

        every { warehouse.stationedVehicles } returns listOf(
            vehicle1,
            vehicle2
        )

        coEvery {
            warehouseRepository.findById("WH-001")
        } returns Result.success(warehouse)

        val useCase = GetWarehouseLoadFactorUseCase(
            warehouseRepository
        )

        // When
        val result = useCase("WH-001")

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(0.5)
    }


    @Test
    fun `should return zero when warehouse has no vehicle capacity`() = runTest {

        // Given
        val warehouseRepository = mockk<WarehouseRepository>()
        val warehouse = mockk<Warehouse>()

        val package1 = mockk<Package>()
        val vehicle = mockk<Vehicle>()

        every { package1.weightByKg } returns 100.0
        every { vehicle.maxCapacityKg } returns 0.0

        every { warehouse.cargoQueue } returns listOf(
            package1
        )

        every { warehouse.stationedVehicles } returns listOf(
            vehicle
        )

        coEvery {
            warehouseRepository.findById("WH-001")
        } returns Result.success(warehouse)

        val useCase = GetWarehouseLoadFactorUseCase(
            warehouseRepository
        )

        // When
        val result = useCase("WH-001")

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(0.0)
    }


    @Test
    fun `should return failure when warehouse repository fails`() = runTest {

        // Given
        val warehouseRepository = mockk<WarehouseRepository>()

        val exception = RuntimeException("Database error")

        coEvery {
            warehouseRepository.findById("WH-001")
        } returns Result.failure(exception)

        val useCase = GetWarehouseLoadFactorUseCase(
            warehouseRepository
        )

        // When
        val result = useCase("WH-001")

        // Then
        assertThat(result.isFailure).isTrue()

        assertThat(result.exceptionOrNull())
            .isEqualTo(exception)
    }
}