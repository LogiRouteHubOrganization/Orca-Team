package dataTest.repositoryTest

import data.dataholder.FleetRaw
import data.datasource.local.LocalVehicleDataSource
import data.datasource.remote.RemoteVehicleDataSource
import data.mapper.VehicleMapper
import data.remote.dto.response.VehicleDto
import domain.model.Vehicle
import domain.model.Warehouse
import domain.model.updateinput.VehicleUpdateModel
import domain.repository.WarehouseRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.Runs
import io.mockk.mockk
import io.mockk.verify
import com.google.common.truth.Truth.assertThat
import data.repository.VehicleRepositoryImpl


class VehicleRepositoryImplTest {

    private lateinit var vehicleDataSource: LocalVehicleDataSource
    private lateinit var vehicleRemoteDataSource: RemoteVehicleDataSource
    private lateinit var vehicleMapper: VehicleMapper
    private lateinit var warehouseRepository: WarehouseRepository

    private lateinit var repository: VehicleRepositoryImpl

    private lateinit var warehouseA: Warehouse
    private lateinit var warehouseB: Warehouse

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        vehicleDataSource = mockk()
        vehicleRemoteDataSource = mockk()
        vehicleMapper = VehicleMapper()
        warehouseRepository = mockk()

        warehouseA = mockk()
        warehouseB = mockk()

        every { warehouseA.id } returns "WH-000001"
        every { warehouseB.id } returns "WH-000002"

        every { warehouseA.addVehicle(any()) } just Runs
        every { warehouseB.addVehicle(any()) } just Runs
        every { warehouseA.removeVehicle(any()) } just Runs
        every { warehouseB.removeVehicle(any()) } just Runs
        every {
            warehouseA.updateVehicleAtIndex(any(), any())
        } just Runs
        every {
            warehouseB.updateVehicleAtIndex(any(), any())
        } just Runs

        coEvery {
            vehicleDataSource.loadVehicles()
        } returns emptyList()

        coEvery {
            warehouseRepository.findById(any())
        } returns Result.success(null)

        repository = VehicleRepositoryImpl(
            vehicleDataSource = vehicleDataSource,
            vehicleRemoteDataSource = vehicleRemoteDataSource,
            vehicleMapper = vehicleMapper,
            warehouseRepository = warehouseRepository
        )
    }


    private fun createRaw(
        id: String = "TRK-000001",
        warehouseId: String = "WH-000001",
        capacity: Double = 1000.0,
        costPerKm: Double = 2.5
    ) = FleetRaw(
        vehicleId = id,
        currentHubId = warehouseId,
        maxCapacityKg = capacity,
        costPerKm = costPerKm
    )

    private fun createVehicle(
        id: String = "TRK-000001",
        warehouse: Warehouse = warehouseA,
        capacity: Double = 1000.0,
        costPerKm: Double = 2.5
    ) = Vehicle(
        id = id,
        maxCapacityKg = capacity,
        costPerKm = costPerKm,
        currentWarehouse = warehouse
    )

    private fun createVehicleDto(
        id: String = "TRK-000001",
        warehouseId: String = "WH-000001",
        capacity: Double = 1000.0,
        costPerKm: Double = 2.5
    ): VehicleDto = mockk {
        every { this@mockk.id } returns id
        every { maxCapacityKg } returns capacity
        every { this@mockk.costPerKm } returns costPerKm
        every { currentWarehouseId } returns warehouseId
    }

    private fun stubWarehouse(
        warehouseId: String,
        warehouse: Warehouse
    ) {
        coEvery {
            warehouseRepository.findById(warehouseId)
        } returns Result.success(warehouse)
    }

    @Test
    fun `findAll should loads local vehicles and returns mapped vehicles`() = runTest {
        // Given
        val rawVehicle = createRaw()
        coEvery {
            vehicleDataSource.loadVehicles()
        } returns listOf(rawVehicle)

        stubWarehouse("WH-000001", warehouseA)

        // When
        val result = repository.findAll()

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).hasSize(1)
        assertThat(result.getOrThrow().first().id)
            .isEqualTo("TRK-000001")

        assertThat(result.getOrThrow().first().currentWarehouse.id)
            .isEqualTo("WH-000001")

        verify(exactly = 1) {
            warehouseA.addVehicle(any())
        }
        coVerify(exactly = 1) {
            vehicleDataSource.loadVehicles()
        }
    }

    @Test
    fun `findAll should returns empty list when no vehicles exist`() = runTest {
        // Given
        coEvery {
            vehicleDataSource.loadVehicles()
        } returns emptyList()

        // When
        val result = repository.findAll()

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEmpty()
    }

    @Test
    fun `findAll should loads local vehicles only once`() = runTest {
        // Given
        coEvery {
            vehicleDataSource.loadVehicles()
        } returns emptyList()

        // When
        repository.findAll()
        repository.findAll()

        // Then
        coVerify(exactly = 1) {
            vehicleDataSource.loadVehicles()
        }
    }

    @Test
    fun `findById returns vehicle when it exists`() = runTest {
        // Given
        coEvery {
            vehicleDataSource.loadVehicles()
        } returns listOf(createRaw())

        stubWarehouse("WH-000001", warehouseA)

        // When
        val result = repository.findById("TRK-000001")

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isNotNull()
        assertThat(result.getOrThrow()!!.id).isEqualTo("TRK-000001")
    }

    @Test
    fun `findById returns null when vehicle does not exist`() = runTest {
        // Given
        coEvery {
            vehicleDataSource.loadVehicles()
        } returns emptyList()

        // When
        val result = repository.findById("TRK-000999")

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isNull()
    }

    @Test
    fun `add creates vehicle remotely and updates local data and warehouse`() =
        runTest {
            // Given
            val vehicle = createVehicle()
            val createdDto = createVehicleDto()

            coEvery {
                vehicleRemoteDataSource.createVehicle(any())
            } returns createdDto

            coEvery {
                vehicleDataSource.addVehicle(any())
            } returns Unit

            stubWarehouse("WH-000001", warehouseA)

            // When
            val result = repository.add(vehicle)

            // Then
            assertThat(result.isSuccess).isTrue()

            val createdVehicle = result.getOrThrow()
            assertThat(createdVehicle.id).isEqualTo(vehicle.id)
            assertThat(createdVehicle.currentWarehouse.id)
                .isEqualTo("WH-000001")

            coVerify(exactly = 1) {
                vehicleRemoteDataSource.createVehicle(any())
                vehicleDataSource.addVehicle(
                    match { it.vehicleId == "TRK-000001" }
                )
            }
            verify(exactly = 1) {
                warehouseA.addVehicle(
                    match { it.id == "TRK-000001" }
                )
            }
        }

    @Test
    fun `update changes vehicle and moves relation to new warehouse`() =
        runTest {
            // Given
            val originalRaw = createRaw()
            val updatedDto = createVehicleDto(
                warehouseId = "WH-000002",
                capacity = 2000.0,
                costPerKm = 3.0
            )

            coEvery {
                vehicleDataSource.loadVehicles()
            } returns listOf(originalRaw)

            stubWarehouse("WH-000001", warehouseA)
            stubWarehouse("WH-000002", warehouseB)

            coEvery {
                vehicleRemoteDataSource.updateVehicle(
                    id = "TRK-000001",
                    vehicleRequest = any()
                )
            } returns updatedDto

            coEvery {
                vehicleDataSource.updateVehicle(any())
            } returns Unit

            val updateModel = VehicleUpdateModel(
                maxCapacityKg = 2000.0,
                costPerKm = 3.0,
                currentWarehouseId = "WH-000002"
            )

            // When
            val result = repository.update(
                vehicleId = "TRK-000001",
                updateModel = updateModel
            )

            // Then
            assertThat(result.isSuccess).isTrue()

            val updatedVehicle = result.getOrThrow()
            assertThat(updatedVehicle.id).isEqualTo("TRK-000001")
            assertThat(updatedVehicle.maxCapacityKg).isEqualTo(2000.0)
            assertThat(updatedVehicle.costPerKm).isEqualTo(3.0)
            assertThat(updatedVehicle.currentWarehouse.id)
                .isEqualTo("WH-000002")

            verify(exactly = 1) {
                warehouseA.removeVehicle(
                    match { it.id == "TRK-000001" }
                )
                warehouseB.addVehicle(
                    match { it.id == "TRK-000001" }
                )
            }


            coVerify(exactly = 1) {
                vehicleDataSource.updateVehicle(
                    match {
                        it.vehicleId == "TRK-000001" &&
                                it.currentHubId == "WH-000002"
                    }
                )
            }
        }

    @Test
    fun `update returns failure when vehicle does not exist`() = runTest {
        // Given
        coEvery {
            vehicleDataSource.loadVehicles()
        } returns emptyList()

        // When
        val result = repository.update(
            vehicleId = "TRK-999999",
            updateModel = VehicleUpdateModel(maxCapacityKg = 2000.0)
        )

        // Then
        assertThat(result.isFailure).isTrue()

        coVerify(exactly = 0) {
            vehicleRemoteDataSource.updateVehicle(any(), any())
        }
    }

    @Test
    fun `delete removes vehicle remotely locally and from warehouse`() =
        runTest {
            // Given
            coEvery {
                vehicleDataSource.loadVehicles()
            } returns listOf(createRaw())

            stubWarehouse("WH-000001", warehouseA)

            coEvery {
                vehicleRemoteDataSource.deleteVehicle("TRK-000001")
            } returns Unit

            coEvery {
                vehicleDataSource.deleteVehicle("TRK-000001")
            } returns Unit

            // When
            val result = repository.delete("TRK-000001")

            // Then
            assertThat(result.isSuccess).isTrue()

            coVerify(exactly = 1) {
                vehicleRemoteDataSource.deleteVehicle("TRK-000001")
                vehicleDataSource.deleteVehicle("TRK-000001")
            }

            verify(exactly = 1) {
                warehouseA.removeVehicle(
                    match { it.id == "TRK-000001" }
                )
            }

            val remainingVehicles = repository.findAll().getOrThrow()
            assertThat(remainingVehicles).isEmpty()
        }

    @Test
    fun `refresh fetches remote vehicles and replaces local data`() =
        runTest {
            // Given
            val remoteDto = createVehicleDto()

            coEvery {
                vehicleRemoteDataSource.getAllVehicles()
            } returns listOf(remoteDto)

            coEvery {
                vehicleDataSource.replaceVehicles(any())
            } returns Unit

            // When
            val result = repository.refresh()

            // Then
            assertThat(result.isSuccess).isTrue()

            coVerify(exactly = 1) {
                vehicleRemoteDataSource.getAllVehicles()
                vehicleDataSource.replaceVehicles(
                    listOf(createRaw())
                )
            }
        }

    @Test
    fun `initializeStationedVehicles loads and associates vehicles`() =
        runTest {
            // Given
            coEvery {
                vehicleDataSource.loadVehicles()
            } returns listOf(createRaw())

            stubWarehouse("WH-000001", warehouseA)

            // When
            val result = repository.initializeStationedVehicles()

            // Then
            assertThat(result.isSuccess).isTrue()

            verify(exactly = 1) {
                warehouseA.addVehicle(
                    match { it.id == "TRK-000001" }
                )
            }

            coVerify(exactly = 1) {
                vehicleDataSource.loadVehicles()
            }
        }
}

