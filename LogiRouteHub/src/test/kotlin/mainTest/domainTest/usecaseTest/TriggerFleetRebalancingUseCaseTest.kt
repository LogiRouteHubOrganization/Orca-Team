package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Vehicle
import domain.model.Warehouse
import domain.model.request.FleetRebalancingConfig
import domain.repository.WarehouseRepository
import domain.usecase.GetWarehouseLoadFactorUseCase
import domain.usecase.TriggerFleetRebalancingUseCase
import io.mockk.mockk
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class TriggerFleetRebalancingUseCaseTest {
    private lateinit var warehouseRepository: WarehouseRepository
    private lateinit var getWarehouseLoadFactorUseCase: GetWarehouseLoadFactorUseCase
    private lateinit var useCase: TriggerFleetRebalancingUseCase

    @BeforeEach
    fun setUp() {
        warehouseRepository = mockk()
        getWarehouseLoadFactorUseCase = mockk()

        useCase = TriggerFleetRebalancingUseCase(
            warehouseRepository = warehouseRepository,
            getWarehouseLoadFactorUseCase = getWarehouseLoadFactorUseCase
        )
    }

    @Test
    fun shouldCreateRebalancingPlanWhenUnderutilizedWarehouseHasEmptyVehicle() = runTest {
        val sourceWarehouse = mockk<Warehouse>()
        val overloadedWarehouse = mockk<Warehouse>()
        val emptyVehicle = mockk<Vehicle>()

        every { sourceWarehouse.id } returns "WH-000001"
        every { overloadedWarehouse.id } returns "WH-000004"
        every { sourceWarehouse.stationedVehicles
                } returns listOf(emptyVehicle)

        every { emptyVehicle.calculateTotalLoadedWeightKg()
                } returns 0.0

        coEvery { warehouseRepository.findAll() } returns
                Result.success(listOf(sourceWarehouse, overloadedWarehouse))

        coEvery {
            getWarehouseLoadFactorUseCase("WH-000001")
        } returns Result.success(0.3)

        coEvery {
            getWarehouseLoadFactorUseCase("WH-000004")
        } returns Result.success(2.0)

        val config = FleetRebalancingConfig(
            highLoadFactorThreshold = 1.5,
            lowLoadFactorThreshold = 0.5,
            maximumVehiclesToRebalance = 1
        )

        val result = useCase(config)

        assertThat(result.isSuccess).isTrue()

        val plans = result.getOrThrow()

        assertThat(plans).hasSize(1)
        assertThat(plans[0].sourceWarehouse).isEqualTo(sourceWarehouse)
        assertThat(plans[0].overloadedWarehouse).isEqualTo(overloadedWarehouse)
        assertThat(plans[0].vehiclesToMove).containsExactly(emptyVehicle)
    }

    @Test
    fun shouldReturnNoPlansWhenSourceWarehouseHasNoEmptyVehicles() = runTest {
        val sourceWarehouse = mockk<Warehouse>()
        val overloadedWarehouse = mockk<Warehouse>()
        val loadedVehicle = mockk<Vehicle>()

        every { sourceWarehouse.id } returns "WH-000001"
        every { overloadedWarehouse.id } returns "WH-000002"
        every { sourceWarehouse.stationedVehicles
        } returns listOf(loadedVehicle)

        every { loadedVehicle.calculateTotalLoadedWeightKg()
        } returns 100.0

        coEvery { warehouseRepository.findAll() } returns
                Result.success(listOf(sourceWarehouse, overloadedWarehouse))

        coEvery { getWarehouseLoadFactorUseCase("WH-000001")
        } returns Result.success(0.3)

        coEvery { getWarehouseLoadFactorUseCase("WH-000002")
        } returns Result.success(2.0)

        val result = useCase(
            FleetRebalancingConfig(
                1.5,
                0.5,
                1)
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEmpty()
    }

    @Test
    fun shouldNotCreatePlanForSameSourceAndTargetWarehouse() = runTest {
        val warehouse = mockk<Warehouse>()
        val emptyVehicle = mockk<Vehicle>()

        every { warehouse.id } returns "WH-000003"
        every { warehouse.stationedVehicles
        } returns listOf(emptyVehicle)

        every { emptyVehicle.calculateTotalLoadedWeightKg()
        } returns 0.0

        coEvery { warehouseRepository.findAll() } returns
                Result.success(listOf(warehouse))

        coEvery { getWarehouseLoadFactorUseCase("WH-000003")
        } returns Result.success(0.5)

        val result = useCase(
            FleetRebalancingConfig(
                highLoadFactorThreshold = 0.5,
                lowLoadFactorThreshold = 0.5,
                maximumVehiclesToRebalance = 1
            )
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEmpty()
    }

    @Test
    fun shouldReturnNoPlansWhenThereAreNoOverloadedWarehouses() = runTest {
        val sourceWarehouse = mockk<Warehouse>()
        val emptyVehicle = mockk<Vehicle>()

        every { sourceWarehouse.id } returns "WH-000001"
        every { sourceWarehouse.stationedVehicles
        } returns listOf(emptyVehicle)

        every { emptyVehicle.calculateTotalLoadedWeightKg()
        } returns 0.0

        coEvery { warehouseRepository.findAll() } returns
                Result.success(listOf(sourceWarehouse))

        coEvery { getWarehouseLoadFactorUseCase("WH-000001")
        } returns Result.success(0.3)

        val result = useCase(
            FleetRebalancingConfig(
                1.5,
                0.5,
                1)
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEmpty()
    }

    @Test
    fun shouldReturnNoPlansWhenThereAreNoUnderutilizedWarehouses() = runTest {
        val overloadedWarehouse = mockk<Warehouse>()

        every { overloadedWarehouse.id } returns "WH-000005"

        coEvery { warehouseRepository.findAll() } returns
                Result.success(listOf(overloadedWarehouse))

        coEvery { getWarehouseLoadFactorUseCase("WH-000005")
        } returns Result.success(2.0)

        val result = useCase(
            FleetRebalancingConfig(
                  1.5
                , 0.5,
                 1)
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEmpty()
    }

    @Test
    fun shouldReturnNoPlansWhenMaximumVehiclesToRebalanceIsZero() = runTest {
        val sourceWarehouse = mockk<Warehouse>()
        val overloadedWarehouse = mockk<Warehouse>()
        val emptyVehicle = mockk<Vehicle>()

        every { sourceWarehouse.id } returns "WH-000002"
        every { overloadedWarehouse.id } returns "WH-000003"
        every { sourceWarehouse.stationedVehicles
        } returns listOf(emptyVehicle)

        every { emptyVehicle.calculateTotalLoadedWeightKg()
        } returns 0.0

        coEvery { warehouseRepository.findAll() } returns
                Result.success(listOf(sourceWarehouse, overloadedWarehouse))

        coEvery { getWarehouseLoadFactorUseCase("WH-000002")
        } returns Result.success(0.3)
        coEvery { getWarehouseLoadFactorUseCase("WH-000003")
        } returns Result.success(2.0)

        val result = useCase(
            FleetRebalancingConfig(
                1.5,
                0.5,
                0)
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEmpty()
    }

    @Test
    fun shouldMoveOnlyMaximumConfiguredNumberOfEmptyVehicles() = runTest {
        val sourceWarehouse = mockk<Warehouse>()
        val overloadedWarehouse = mockk<Warehouse>()

        val vehicle1 = mockk<Vehicle>()
        val vehicle2 = mockk<Vehicle>()
        val vehicle3 = mockk<Vehicle>()

        every { sourceWarehouse.id } returns "WH-000001"
        every { overloadedWarehouse.id } returns "WH-000002"
        every {
            sourceWarehouse.stationedVehicles
        } returns listOf(vehicle1, vehicle2, vehicle3)

        every { vehicle1.calculateTotalLoadedWeightKg() } returns 0.0
        every { vehicle2.calculateTotalLoadedWeightKg() } returns 0.0
        every { vehicle3.calculateTotalLoadedWeightKg() } returns 0.0

        coEvery { warehouseRepository.findAll() } returns
                Result.success(listOf(sourceWarehouse, overloadedWarehouse))

        coEvery { getWarehouseLoadFactorUseCase("WH-000001")
        } returns Result.success(0.3)

        coEvery { getWarehouseLoadFactorUseCase("WH-000002")
        } returns Result.success(2.0)

        val result = useCase(
            FleetRebalancingConfig(1.5, 0.5, 2)
        )

        val plan = result.getOrThrow().single()

        assertThat(plan.vehiclesToMove).hasSize(2)
        assertThat(plan.vehiclesToMove).
        containsExactly(vehicle1, vehicle2)
    }

    @Test
    fun shouldReturnFailureWhenWarehouseRepositoryFails() = runTest {
        val exception = RuntimeException("Failed to load warehouses")

        coEvery {
            warehouseRepository.findAll()
        } returns Result.failure(exception)

        val result = useCase(
            FleetRebalancingConfig(
                1.5,
                0.5,
                1)
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun shouldReturnFailureWhenLoadFactorCalculationFails() = runTest {
        val warehouse = mockk<Warehouse>()
        val exception = RuntimeException("Load factor calculation failed")

        every { warehouse.id } returns "WH-000001"

        coEvery {
            warehouseRepository.findAll()
        } returns Result.success(listOf(warehouse))

        coEvery {
            getWarehouseLoadFactorUseCase("WH-000001")
        } returns Result.failure(exception)

        val result = useCase(
            FleetRebalancingConfig(
                1.5,
                0.5,
                1)
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }



}

