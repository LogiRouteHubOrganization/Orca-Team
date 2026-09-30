package main.domain.usecase

import io.mockk.mockk
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
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


class AddVehicleToHubUseCaseTest {

    @Test
    fun `should add vehicle to warehouse successfully`() = runTest {

        // Given
        val warehouseRepository = mockk<WarehouseRepository>()
        val vehicleRepository = mockk<VehicleRepository>()
        val updateVehicleValidator = mockk<UpdateVehicleValidator>()

        val useCase = AddVehicleToHubUseCase(
            warehouseRepository = warehouseRepository,
            vehicleRepository = vehicleRepository,
            updateVehicleValidator = updateVehicleValidator
        )

        val oldWarehouse = Warehouse(
            id = "WH-000002",
            name = "Old Warehouse",
            regionalZone = "South",
            latitude = 31.4,
            longitude = 34.3
        )

        val warehouse = Warehouse(
            id = "WH-000001",
            name = "Main Warehouse",
            regionalZone = "North",
            latitude = 31.5,
            longitude = 34.4
        )

        val vehicle = Vehicle(
            id = "TRK-000001",
            maxCapacityKg = 1000.0,
            costPerKm = 2.5,
            currentWarehouse = oldWarehouse
        )

        val updatedVehicle = vehicle.copy(currentWarehouse = warehouse)

        val vehicleUpdateModel = VehicleUpdateModel(currentWarehouseId = warehouse.id)

        coEvery {
            warehouseRepository.findById(warehouse.id)
        } returns Result.success(warehouse)

        coEvery {
            vehicleRepository.findById(vehicle.id)
        } returns Result.success(vehicle)

        every {
            updateVehicleValidator(vehicleUpdateModel)
        } returns ResultValidator.Valid

        coEvery {
            vehicleRepository.update(
                vehicle.id,
                vehicleUpdateModel
            )
        } returns Result.success(updatedVehicle)

        // When
        val result = useCase(
            warehouseId = warehouse.id,
            vehicleId = vehicle.id
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isSameInstanceAs(warehouse)

        assertThat(warehouse.stationedVehicles)
            .containsExactly(updatedVehicle)


        coVerify(exactly = 1) {
            warehouseRepository.findById(warehouse.id)
        }

        coVerify(exactly = 1) {
            vehicleRepository.findById(vehicle.id)
        }

        coVerify(exactly = 1) {
            vehicleRepository.update(
                vehicle.id,
                vehicleUpdateModel
            )
        }
    }

    @Test
    fun `should return null when warehouse does not exist`() = runTest {

        // Given
        val warehouseRepository = mockk<WarehouseRepository>()
        val vehicleRepository = mockk<VehicleRepository>()
        val updateVehicleValidator = mockk<UpdateVehicleValidator>()

        val useCase = AddVehicleToHubUseCase(
            warehouseRepository = warehouseRepository,
            vehicleRepository = vehicleRepository,
            updateVehicleValidator = updateVehicleValidator
        )

        val warehouseId = "WH-000001"
        val vehicleId = "TRK-000001"

        coEvery {
            warehouseRepository.findById(warehouseId)
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
        }

        coVerify(exactly = 0) {
            updateVehicleValidator(any())
        }

        coVerify(exactly = 0) {
            vehicleRepository.update(any(), any())
        }
    }

    @Test
    fun `should return null when vehicle does not exist`() = runTest {

        // Given
        val warehouseRepository = mockk<WarehouseRepository>()
        val vehicleRepository = mockk<VehicleRepository>()
        val updateVehicleValidator = mockk<UpdateVehicleValidator>()

        val useCase = AddVehicleToHubUseCase(
            warehouseRepository = warehouseRepository,
            vehicleRepository = vehicleRepository,
            updateVehicleValidator = updateVehicleValidator
        )

        val warehouse = Warehouse(
            id = "WH-000001",
            name = "Main Warehouse",
            regionalZone = "North",
            latitude = 31.5,
            longitude = 34.4
        )

        val vehicleId = "TRK-000001"

        coEvery {
            warehouseRepository.findById(warehouse.id)
        } returns Result.success(warehouse)

        coEvery {
            vehicleRepository.findById(vehicleId)
        } returns Result.success(null)

        // When
        val result = useCase(
            warehouseId = warehouse.id,
            vehicleId = vehicleId
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNull()

        coVerify(exactly = 1) {
            warehouseRepository.findById(warehouse.id)
        }

        coVerify(exactly = 1) {
            vehicleRepository.findById(vehicleId)
        }

        coVerify(exactly = 0) {
            updateVehicleValidator(any())
        }

        coVerify(exactly = 0) {
            vehicleRepository.update(any(), any())
        }
    }

    @Test
    fun `should return failure when vehicle update validation fails`() = runTest {

        // Given
        val warehouseRepository = mockk<WarehouseRepository>()
        val vehicleRepository = mockk<VehicleRepository>()
        val updateVehicleValidator = mockk<UpdateVehicleValidator>()

        val useCase = AddVehicleToHubUseCase(
            warehouseRepository = warehouseRepository,
            vehicleRepository = vehicleRepository,
            updateVehicleValidator = updateVehicleValidator
        )

        val warehouse = Warehouse(
            id = "WH-000001",
            name = "Main Warehouse",
            regionalZone = "South",
            latitude = 30.5,
            longitude = 35.4
        )

        val vehicle = Vehicle(
            id = "TRK-000001",
            maxCapacityKg = 2000.0,
            costPerKm = 2.5,
            currentWarehouse = warehouse
        )

        val vehicleUpdateModel = VehicleUpdateModel(
            currentWarehouseId = warehouse.id
        )

        val invalidValidation = ResultValidator.Invalid(
            errors = listOf(
                FieldValidator(
                    field = "currentWarehouseId",
                    error = ErrorValidator.InvalidIdPrefix(setOf("WH-"))
                )
            )
        )

        coEvery {
            warehouseRepository.findById(warehouse.id)
        } returns Result.success(warehouse)

        coEvery {
            vehicleRepository.findById(vehicle.id)
        } returns Result.success(vehicle)

        every {
            updateVehicleValidator(vehicleUpdateModel)
        } returns invalidValidation

        // When
        val result = useCase(
            warehouseId = warehouse.id,
            vehicleId = vehicle.id
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(EntityValidationException::class.java)

        coVerify(exactly = 1) {
            warehouseRepository.findById(warehouse.id)
        }

        coVerify(exactly = 1) {
            vehicleRepository.findById(vehicle.id)
        }

        coVerify(exactly = 1) {
            updateVehicleValidator(vehicleUpdateModel)
        }

        coVerify(exactly = 0) {
            vehicleRepository.update(any(), any())
        }
    }

    @Test
    fun `should return failure when vehicle update fails`() = runTest {

        // Given
        val warehouseRepository = mockk<WarehouseRepository>()
        val vehicleRepository = mockk<VehicleRepository>()
        val updateVehicleValidator = mockk<UpdateVehicleValidator>()

        val useCase = AddVehicleToHubUseCase(
            warehouseRepository = warehouseRepository,
            vehicleRepository = vehicleRepository,
            updateVehicleValidator = updateVehicleValidator
        )

        val warehouse = Warehouse(
            id = "WH-000001",
            name = "Main Warehouse",
            regionalZone = "North",
            latitude = 21.5,
            longitude = 50.0
        )

        val vehicle = Vehicle(
            id = "TRK-000001",
            maxCapacityKg = 1000.0,
            costPerKm = 5.0,
            currentWarehouse = warehouse
        )

        val vehicleUpdateModel = VehicleUpdateModel(
            currentWarehouseId = warehouse.id
        )

        val updateException = RuntimeException("Vehicle update failed")

        coEvery {
            warehouseRepository.findById(warehouse.id)
        } returns Result.success(warehouse)

        coEvery {
            vehicleRepository.findById(vehicle.id)
        } returns Result.success(vehicle)

        every {
            updateVehicleValidator(vehicleUpdateModel)
        } returns ResultValidator.Valid

        coEvery {
            vehicleRepository.update(
                vehicle.id,
                vehicleUpdateModel
            )
        } returns Result.failure(updateException)

        // When
        val result = useCase(
            warehouseId = warehouse.id,
            vehicleId = vehicle.id
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isSameInstanceAs(updateException)


        coVerify(exactly = 1) {
            warehouseRepository.findById(warehouse.id)
        }

        coVerify(exactly = 1) {
            vehicleRepository.findById(vehicle.id)
        }

        coVerify(exactly = 1) {
            updateVehicleValidator(vehicleUpdateModel)
        }

        coVerify(exactly = 1) {
            vehicleRepository.update(
                vehicle.id,
                vehicleUpdateModel
            )
        }
        assertThat(warehouse.stationedVehicles).isEmpty()
    }





}
