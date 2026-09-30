package main.domain.usecase

import com.google.common.truth.Truth.assertThat
import domain.model.Vehicle
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.usecase.FindStationedVehiclesByCapacityUseCase
import domain.usecase.RecommendVehicleByCapacityAndCostUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RecommendVehicleByCapacityAndCostUseCaseTest {

    @Test
    fun `should recommend vehicle with least remaining capacity after load`() = runTest {

        // Given
        val findStationedVehiclesByCapacityUseCase =
            mockk<FindStationedVehiclesByCapacityUseCase>()

        val recommendVehicleByCapacityAndCostUseCase =
            RecommendVehicleByCapacityAndCostUseCase(
                findStationedVehiclesByCapacityUseCase =
                    findStationedVehiclesByCapacityUseCase
            )

        val warehouseId = "WH-000001"
        val requiredLoadKg = 50.0

        val vehicle1 = mockk<Vehicle>()
        val vehicle2 = mockk<Vehicle>()

        every { vehicle1.calculateTotalLoadedWeightKg() } returns 20.0
        every { vehicle1.maxCapacityKg } returns 100.0
        every { vehicle1.costPerKm } returns 5.0

        every { vehicle2.calculateTotalLoadedWeightKg() } returns 20.0
        every { vehicle2.maxCapacityKg } returns 120.0
        every { vehicle2.costPerKm } returns 3.0

        coEvery {
            findStationedVehiclesByCapacityUseCase(
                warehouseId = warehouseId,
                requiredLoadKg = requiredLoadKg
            )
        } returns Result.success(listOf(vehicle1, vehicle2))


        // When
        val result = recommendVehicleByCapacityAndCostUseCase(
            warehouseId = warehouseId,
            requiredLoadKg = requiredLoadKg
        )

        // Then
        assertThat(result).isEqualTo(
            Result.success(vehicle1)
        )
    }

    @Test
    fun `should recommend cheaper vehicle when remaining capacity is equal`() = runTest {

        // Given
        val findStationedVehiclesByCapacityUseCase =
            mockk<FindStationedVehiclesByCapacityUseCase>()

        val recommendVehicleByCapacityAndCostUseCase =
            RecommendVehicleByCapacityAndCostUseCase(
                findStationedVehiclesByCapacityUseCase =
                    findStationedVehiclesByCapacityUseCase
            )

        val warehouseId = "WH-000001"
        val requiredLoadKg = 50.0

        val expensiveVehicle = mockk<Vehicle>()
        val cheapVehicle = mockk<Vehicle>()

        every { expensiveVehicle.calculateTotalLoadedWeightKg() } returns 20.0
        every { expensiveVehicle.maxCapacityKg } returns 100.0
        every { expensiveVehicle.costPerKm } returns 7.0

        every { cheapVehicle.calculateTotalLoadedWeightKg() } returns 20.0
        every { cheapVehicle.maxCapacityKg } returns 100.0
        every { cheapVehicle.costPerKm } returns 5.0

        coEvery {
            findStationedVehiclesByCapacityUseCase(
                warehouseId = warehouseId,
                requiredLoadKg = requiredLoadKg
            )
        } returns Result.success(
            listOf(expensiveVehicle, cheapVehicle)
        )



        // When
        val result = recommendVehicleByCapacityAndCostUseCase(
            warehouseId = warehouseId,
            requiredLoadKg = requiredLoadKg
        )

        // Then
        assertThat(result).isEqualTo(
            Result.success(cheapVehicle)
        )
    }

    @Test
    fun `should consider current vehicle load when calculating remaining capacity`() = runTest {

        // Given
        val findStationedVehiclesByCapacityUseCase =
            mockk<FindStationedVehiclesByCapacityUseCase>()

        val recommendVehicleByCapacityAndCostUseCase =
            RecommendVehicleByCapacityAndCostUseCase(
                findStationedVehiclesByCapacityUseCase =
                    findStationedVehiclesByCapacityUseCase
            )

        val warehouseId = "WH-000001"
        val requiredLoadKg = 30.0

        val partiallyLoadedVehicle = mockk<Vehicle>()
        val largerVehicle = mockk<Vehicle>()

        every {
            partiallyLoadedVehicle.calculateTotalLoadedWeightKg()
        } returns 40.0

        every {
            partiallyLoadedVehicle.maxCapacityKg
        } returns 100.0

        every {
            partiallyLoadedVehicle.costPerKm
        } returns 5.0

        every {
            largerVehicle.calculateTotalLoadedWeightKg()
        } returns 0.0

        every {
            largerVehicle.maxCapacityKg
        } returns 110.0

        every {
            largerVehicle.costPerKm
        } returns 5.0

        coEvery {
            findStationedVehiclesByCapacityUseCase(
                warehouseId = warehouseId,
                requiredLoadKg = requiredLoadKg
            )
        } returns Result.success(
            listOf(partiallyLoadedVehicle, largerVehicle)
        )


        // When
        val result = recommendVehicleByCapacityAndCostUseCase(
            warehouseId = warehouseId,
            requiredLoadKg = requiredLoadKg
        )

        // Then
        assertThat(result).isEqualTo(
            Result.success(partiallyLoadedVehicle)
        )
    }

    @Test
    fun `should return null when no suitable vehicles are available`() = runTest {

        // Given
        val findStationedVehiclesByCapacityUseCase =
            mockk<FindStationedVehiclesByCapacityUseCase>()

        val recommendVehicleByCapacityAndCostUseCase =
            RecommendVehicleByCapacityAndCostUseCase(
                findStationedVehiclesByCapacityUseCase =
                    findStationedVehiclesByCapacityUseCase
            )

        val warehouseId = "WH-000001"
        val requiredLoadKg = 50.0

        coEvery {
            findStationedVehiclesByCapacityUseCase(
                warehouseId = warehouseId,
                requiredLoadKg = requiredLoadKg
            )
        } returns Result.success(emptyList<Vehicle>())

        // When
        val result = recommendVehicleByCapacityAndCostUseCase(
            warehouseId = warehouseId,
            requiredLoadKg = requiredLoadKg
        )

        // Then
        assertThat(result).isEqualTo(
            Result.success(null)
        )
    }

    @Test
    fun `should recommend the only suitable vehicle`() = runTest {

        // Given
        val findStationedVehiclesByCapacityUseCase =
            mockk<FindStationedVehiclesByCapacityUseCase>()

        val recommendVehicleByCapacityAndCostUseCase =
            RecommendVehicleByCapacityAndCostUseCase(
                findStationedVehiclesByCapacityUseCase =
                    findStationedVehiclesByCapacityUseCase
            )

        val warehouseId = "WH-000001"
        val requiredLoadKg = 40.0

        val vehicle = mockk<Vehicle>()

        every { vehicle.calculateTotalLoadedWeightKg() } returns 20.0
        every { vehicle.maxCapacityKg } returns 100.0
        every { vehicle.costPerKm } returns 5.0

        coEvery {
            findStationedVehiclesByCapacityUseCase(
                warehouseId = warehouseId,
                requiredLoadKg = requiredLoadKg
            )
        } returns Result.success(listOf(vehicle))

        // When
        val result = recommendVehicleByCapacityAndCostUseCase(
            warehouseId = warehouseId,
            requiredLoadKg = requiredLoadKg
        )

        // Then
        assertThat(result).isEqualTo(
            Result.success(vehicle)
        )
    }

    @Test
    fun `should return failure when finding suitable vehicles fails`() = runTest {

        // Given
        val findStationedVehiclesByCapacityUseCase =
            mockk<FindStationedVehiclesByCapacityUseCase>()

        val recommendVehicleByCapacityAndCostUseCase =
            RecommendVehicleByCapacityAndCostUseCase(
                findStationedVehiclesByCapacityUseCase =
                    findStationedVehiclesByCapacityUseCase
            )

        val warehouseId = "WH-000001"
        val requiredLoadKg = 50.0

        val exception = RuntimeException("Failed to find suitable vehicles")

        coEvery {
            findStationedVehiclesByCapacityUseCase(
                warehouseId = warehouseId,
                requiredLoadKg = requiredLoadKg
            )
        } returns Result.failure(exception)

        // When
        val result = recommendVehicleByCapacityAndCostUseCase(
            warehouseId = warehouseId,
            requiredLoadKg = requiredLoadKg
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }
}
