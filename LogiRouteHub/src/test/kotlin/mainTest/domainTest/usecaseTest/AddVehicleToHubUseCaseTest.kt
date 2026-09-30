package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.exception.EntityValidationException
import domain.model.Vehicle
import domain.model.Warehouse
import domain.model.updateinput.VehicleUpdateModel
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository
import domain.usecase.AddVehicleToHubUseCase
import domain.validator.ErrorValidator
import domain.validator.FieldValidator
import domain.validator.ResultValidator
import domain.validator.vehicle.UpdateVehicleValidator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AddVehicleToHubUseCaseTest {

    private val warehouseRepository = mockk<WarehouseRepository>()
    private val vehicleRepository = mockk<VehicleRepository>()
    private val updateVehicleValidator = mockk<UpdateVehicleValidator>()

    private lateinit var useCase: AddVehicleToHubUseCase

    @BeforeEach
    fun setUp() {
        useCase = AddVehicleToHubUseCase(
            warehouseRepository = warehouseRepository,
            vehicleRepository = vehicleRepository,
            updateVehicleValidator = updateVehicleValidator
        )
    }

    @Test
    fun `should add vehicle to warehouse successfully`() = runTest {
        // Given
        val oldWarehouse = createWarehouse(id = "WH-000002", name = "Old Warehouse")

        val targetWarehouse = createWarehouse(id = "WH-000001", name = "Main Warehouse")

        val vehicle = createVehicle(id = "TRK-000001", warehouse = oldWarehouse)

        val updatedVehicle = vehicle.copy(currentWarehouse = targetWarehouse)

        val updateModel = VehicleUpdateModel(currentWarehouseId = targetWarehouse.id)

        coEvery { warehouseRepository.findById(targetWarehouse.id)
        } returns Result.success(targetWarehouse)

        coEvery { vehicleRepository.findById(vehicle.id)
        } returns Result.success(vehicle)

        every { updateVehicleValidator(updateModel)
        } returns ResultValidator.Valid

        coEvery { vehicleRepository.update(vehicle.id, updateModel)
        } returns Result.success(updatedVehicle)

        // When
        val result = useCase(
            warehouseId = targetWarehouse.id,
            vehicleId = vehicle.id
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull())
            .isSameInstanceAs(targetWarehouse)
        assertThat(targetWarehouse.stationedVehicles)
            .containsExactly(updatedVehicle)

        coVerify(exactly = 1) {
            warehouseRepository.findById(targetWarehouse.id)
            vehicleRepository.findById(vehicle.id)
            vehicleRepository.update(vehicle.id, updateModel)
        }
    }

    @Test
    fun `should return null when warehouse does not exist`() = runTest {
        // Given
        val warehouseId = "WH-000001"
        val vehicleId = "TRK-000001"

        coEvery { warehouseRepository.findById(warehouseId)
        } returns Result.success(null)

        // When
        val result = useCase(
            warehouseId = warehouseId,
            vehicleId = vehicleId
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNull()

        coVerify(exactly = 1) {
            warehouseRepository.findById(warehouseId)
        }
        coVerify(exactly = 0) {
            vehicleRepository.findById(any())
            vehicleRepository.update(any(), any())
        }

        verify(exactly = 0) {
            updateVehicleValidator(any())
        }
    }

    @Test
    fun `should return null when vehicle does not exist`() = runTest {
        // Given
        val warehouse = createWarehouse(id = "WH-000001", name = "Main Warehouse")
        val vehicleId = "TRK-000001"

        coEvery { warehouseRepository.findById(warehouse.id)
        } returns Result.success(warehouse)

        coEvery { vehicleRepository.findById(vehicleId)
        } returns Result.success(null)

        // When
        val result = useCase(warehouseId = warehouse.id,
            vehicleId = vehicleId
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNull()

        coVerify(exactly = 1) {
            warehouseRepository.findById(warehouse.id)
            vehicleRepository.findById(vehicleId)
        }

        coVerify(exactly = 0) {
            updateVehicleValidator(any())
            vehicleRepository.update(any(), any())
        }
    }

    @Test
    fun `should return failure when vehicle update validation fails`() = runTest {
        // Given
        val warehouse = createWarehouse(id = "WH-000001", name = "Main Warehouse")

        val vehicle = createVehicle(id = "TRK-000001", warehouse = warehouse)

        val updateModel = VehicleUpdateModel(currentWarehouseId = warehouse.id)

        val invalidValidation = ResultValidator.Invalid(
            errors = listOf(FieldValidator(
                    field = "currentWarehouseId",
                    error = ErrorValidator.InvalidIdPrefix(setOf("WH-"))
                )
            )
        )
        coEvery { warehouseRepository.findById(warehouse.id)
        } returns Result.success(warehouse)

        coEvery { vehicleRepository.findById(vehicle.id)
        } returns Result.success(vehicle)

        every { updateVehicleValidator(updateModel)
        } returns invalidValidation

        // When
        val result = useCase(warehouseId = warehouse.id, vehicleId = vehicle.id)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(EntityValidationException::class.java)

        coVerify(exactly = 1) {
            warehouseRepository.findById(warehouse.id)
            vehicleRepository.findById(vehicle.id)
            updateVehicleValidator(updateModel)
        }
        coVerify(exactly = 0) {
            vehicleRepository.update(any(), any())
        }
    }

    @Test
    fun `should return failure when vehicle update fails`() = runTest {
        // Given
        val warehouse = createWarehouse(id = "WH-000001", name = "Main Warehouse")

        val vehicle = createVehicle(id = "TRK-000001", warehouse = warehouse)

        val updateModel = VehicleUpdateModel(currentWarehouseId = warehouse.id)

        val updateException = RuntimeException("Vehicle update failed")

        coEvery { warehouseRepository.findById(warehouse.id)
        } returns Result.success(warehouse)

        coEvery { vehicleRepository.findById(vehicle.id)
        } returns Result.success(vehicle)

        every { updateVehicleValidator(updateModel)
        } returns ResultValidator.Valid

        coEvery {
            vehicleRepository.update(vehicle.id, updateModel)
        } returns Result.failure(updateException)

        // When
        val result = useCase(warehouseId = warehouse.id,
            vehicleId = vehicle.id
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isSameInstanceAs(updateException)
        assertThat(warehouse.stationedVehicles).isEmpty()

        coVerify(exactly = 1) {
            warehouseRepository.findById(warehouse.id)
            vehicleRepository.findById(vehicle.id)
            updateVehicleValidator(updateModel)
            vehicleRepository.update(vehicle.id, updateModel)
        }
    }

    private fun createWarehouse(id: String, name: String
    ): Warehouse {
        return Warehouse(
            id = id,
            name = name,
            regionalZone = "North",
            latitude = 22.5,
            longitude = 44.8
        )
    }

    private fun createVehicle(id: String, warehouse: Warehouse
    ): Vehicle {
        return Vehicle(
            id = id,
            maxCapacityKg = 3000.0,
            costPerKm = 3.6,
            currentWarehouse = warehouse
        )
    }
}
