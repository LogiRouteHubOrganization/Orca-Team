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
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
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

    @Test
    fun `should fetch warehouse and vehicle concurrently`() = runTest {
        // Given
        val warehouse = createWarehouse(id = "WH-000001", name = "Main Warehouse")
        val vehicle = createVehicle(id = "TRK-000001", warehouse = warehouse)

        val updateModel = VehicleUpdateModel(currentWarehouseId = warehouse.id)
        val updatedVehicle = vehicle.copy(currentWarehouse = warehouse)

        val warehouseStarted = CompletableDeferred<Unit>()
        val vehicleStarted = CompletableDeferred<Unit>()

        val allowWarehouseToFinish = CompletableDeferred<Unit>()
        val allowVehicleToFinish = CompletableDeferred<Unit>()

        coEvery {
            warehouseRepository.findById(warehouse.id)
        } coAnswers {
            warehouseStarted.complete(Unit)
            allowWarehouseToFinish.await()
            Result.success(warehouse)
        }

        coEvery {
            vehicleRepository.findById(vehicle.id)
        } coAnswers {
            vehicleStarted.complete(Unit)
            allowVehicleToFinish.await()
            Result.success(vehicle)
        }
        every {
            updateVehicleValidator(updateModel)
        } returns ResultValidator.Valid

        coEvery {
            vehicleRepository.update(vehicle.id, updateModel)
        } returns Result.success(updatedVehicle)

        // When
        val resultDeferred = async {
            useCase(warehouse.id, vehicle.id)
        }

        // Then
        warehouseStarted.await()
        vehicleStarted.await()

        allowWarehouseToFinish.complete(Unit)
        allowVehicleToFinish.complete(Unit)

        val result = resultDeferred.await()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isSameInstanceAs(warehouse)

        coVerify(exactly = 1) {
            warehouseRepository.findById(warehouse.id)
            vehicleRepository.findById(vehicle.id)
            vehicleRepository.update(vehicle.id, updateModel)
        }
    }

    @Test
    fun `should cancel sibling request when warehouse lookup fails`() = runTest {
        // Given
        val warehouseId = "WH-000001"
        val vehicleId = "TRK-000001"
        val warehouseFailure = RuntimeException("Warehouse lookup failed")

        val warehouseStarted = CompletableDeferred<Unit>()
        val allowWarehouseFailure = CompletableDeferred<Unit>()
        val vehicleRequestStarted = CompletableDeferred<Unit>()
        val vehicleRequestCancelled = CompletableDeferred<Unit>()

        coEvery {
            warehouseRepository.findById(warehouseId)
        } coAnswers {
            warehouseStarted.complete(Unit)
            allowWarehouseFailure.await()
            Result.failure(warehouseFailure)
        }

        coEvery {
            vehicleRepository.findById(vehicleId)
        } coAnswers {
            vehicleRequestStarted.complete(Unit)

            try {
                awaitCancellation()
            } finally {
                vehicleRequestCancelled.complete(Unit)
            }
        }
        // When
        val resultDeferred = async {
            useCase(warehouseId, vehicleId)
        }

        warehouseStarted.await()
        vehicleRequestStarted.await()
        allowWarehouseFailure.complete(Unit)
        val result = resultDeferred.await()

        // Then
        assertThat(result.isFailure).isTrue()

        assertThat(result.exceptionOrNull())
            .hasMessageThat()
            .isEqualTo("Warehouse lookup failed")

        vehicleRequestCancelled.await()

        coVerify(exactly = 1) {
            warehouseRepository.findById(warehouseId)
            vehicleRepository.findById(vehicleId)
        }
        coVerify(exactly = 0) {
            vehicleRepository.update(any(), any())
        }
    }

    @Test
    fun `should cancel sibling request when vehicle lookup fails`() = runTest {
        // Given
        val warehouseId = "WH-000001"
        val vehicleId = "TRK-000001"
        val vehicleFailure = RuntimeException("Vehicle lookup failed")

        val warehouseStarted = CompletableDeferred<Unit>()
        val warehouseCancelled = CompletableDeferred<Unit>()
        val allowVehicleFailure = CompletableDeferred<Unit>()

        coEvery {
            warehouseRepository.findById(warehouseId)
        } coAnswers {
            warehouseStarted.complete(Unit)

            try {
                awaitCancellation()
            } finally {
                warehouseCancelled.complete(Unit)
            }
        }

        coEvery {
            vehicleRepository.findById(vehicleId)
        } coAnswers {
            warehouseStarted.await()
            allowVehicleFailure.await()
            Result.failure(vehicleFailure)
        }

        // When
        val resultDeferred = async {
            useCase(warehouseId, vehicleId)
        }

        warehouseStarted.await()
        allowVehicleFailure.complete(Unit)

        val result = resultDeferred.await()

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message)
            .isEqualTo("Vehicle lookup failed")

        warehouseCancelled.await()

        coVerify(exactly = 1) {
            warehouseRepository.findById(warehouseId)
            vehicleRepository.findById(vehicleId)
        }
        coVerify(exactly = 0) {
            vehicleRepository.update(any(), any())
        }
    }

    @Test
    fun `should update vehicle only after both lookups complete`() = runTest {
        // Given
        val warehouse = createWarehouse("WH-000001", "Main Warehouse")
        val vehicle = createVehicle("TRK-000001", warehouse)
        val updateModel = VehicleUpdateModel(
            currentWarehouseId = warehouse.id
        )
        val updatedVehicle = vehicle.copy(currentWarehouse = warehouse)

        val allowWarehouseToFinish = CompletableDeferred<Unit>()
        val allowVehicleToFinish = CompletableDeferred<Unit>()

        coEvery {
            warehouseRepository.findById(warehouse.id)
        } coAnswers {
            allowWarehouseToFinish.await()
            Result.success(warehouse)
        }

        coEvery {
            vehicleRepository.findById(vehicle.id)
        } coAnswers {
            allowVehicleToFinish.await()
            Result.success(vehicle)
        }

        every {
            updateVehicleValidator(updateModel)
        } returns ResultValidator.Valid

        coEvery {
            vehicleRepository.update(vehicle.id, updateModel)
        } returns Result.success(updatedVehicle)

        // When
        val resultDeferred = async {
            useCase(warehouse.id, vehicle.id)
        }

        allowWarehouseToFinish.complete(Unit)
        runCurrent()

        // Then
        coVerify(exactly = 0) {
            vehicleRepository.update(any(), any())
        }

        // When
        allowVehicleToFinish.complete(Unit)
        val result = resultDeferred.await()

        // Then
        assertThat(result.isSuccess).isTrue()

        coVerify(exactly = 1) {
            vehicleRepository.update(vehicle.id, updateModel)
        }
    }

    @Test
    fun `should cancel all child requests when caller cancels operation`() = runTest {
        // Given
        val warehouseId = "WH-000001"
        val vehicleId = "TRK-000001"

        val warehouseStarted = CompletableDeferred<Unit>()
        val vehicleStarted = CompletableDeferred<Unit>()

        val warehouseCancelled = CompletableDeferred<Unit>()
        val vehicleCancelled = CompletableDeferred<Unit>()

        coEvery {
            warehouseRepository.findById(warehouseId)
        } coAnswers {
            warehouseStarted.complete(Unit)

            try {
                awaitCancellation()
            } finally {
                warehouseCancelled.complete(Unit)
            }
        }
        coEvery {
            vehicleRepository.findById(vehicleId)
        } coAnswers {
            vehicleStarted.complete(Unit)

            try {
                awaitCancellation()
            } finally {
                vehicleCancelled.complete(Unit)
            }
        }
        // When
        val operation = async {
            useCase(warehouseId, vehicleId)
        }

        warehouseStarted.await()
        vehicleStarted.await()
        operation.cancelAndJoin()

        // Then
        assertThat(operation.isCancelled).isTrue()
        warehouseCancelled.await()
        vehicleCancelled.await()

        coVerify(exactly = 1) {
            warehouseRepository.findById(warehouseId)
            vehicleRepository.findById(vehicleId)
        }

        coVerify(exactly = 0) {
            vehicleRepository.update(any(), any())
        }
    }







}

