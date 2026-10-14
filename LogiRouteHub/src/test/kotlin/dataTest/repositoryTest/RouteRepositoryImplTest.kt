package dataTest.repositoryTest

import com.google.common.truth.Truth.assertThat
import data.dataholder.RouteRaw
import data.datasource.local.LocalRouteDataSource
import data.datasource.remote.RemoteRouteDataSource
import data.mapper.RouteMapper
import data.remote.dto.response.RouteDto
import data.repository.RouteRepositoryImpl
import domain.model.Route
import domain.model.Warehouse
import domain.model.updateinput.RouteUpdateModel
import domain.repository.WarehouseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RouteRepositoryImplTest {

    private lateinit var localDataSource: LocalRouteDataSource
    private lateinit var remoteDataSource: RemoteRouteDataSource
    private lateinit var warehouseRepository: WarehouseRepository
    private lateinit var routeMapper: RouteMapper
    private lateinit var repository: RouteRepositoryImpl

    private val originWarehouse = Warehouse(
        id = "WH-000001",
        name = "Gaza Warehouse",
        regionalZone = "North",
        latitude = 51.5,
        longitude = 20.4
    )

    private val destinationWarehouse = Warehouse(
        id = "WH-000002",
        name = "Khan Younis Warehouse",
        regionalZone = "South",
        latitude = 31.3,
        longitude = 44.3
    )

    private val newOriginWarehouse = Warehouse(
        id = "WH-000003",
        name = "Rafah Warehouse",
        regionalZone = "South",
        latitude = 31.2,
        longitude = 34.25
    )

    private val rawRoute = RouteRaw(
        routeId = "RT-000001",
        originHubId = "WH-000001",
        destinationHubId = "WH-000002",
        distanceKm = 25.0,
        typicalDelayMin = 15
    )

    private val routeDto = RouteDto(
        id = "RT-000001",
        originWarehouseId = "WH-000001",
        destinationWarehouseId = "WH-000002",
        distanceKm = 25.0,
        typicalDelayMin = 15
    )

    private fun createRoute(
        id: String = rawRoute.routeId,
        origin: Warehouse = originWarehouse,
        destination: Warehouse = destinationWarehouse,
        distance: Double = rawRoute.distanceKm,
        delay: Int = rawRoute.typicalDelayMin
    ) = Route(
        id = id,
        originWarehouse = origin,
        destinationWarehouse = destination,
        distanceKm = distance,
        typicalDelayMin = delay
    )

    @BeforeEach
    fun setUp() {
        localDataSource = mockk()
        remoteDataSource = mockk()
        warehouseRepository = mockk()
        routeMapper = RouteMapper()

        repository = RouteRepositoryImpl(
            routeDataSource = localDataSource,
            routeRemoteDataSource = remoteDataSource,
            routeMapper = routeMapper,
            warehouseRepository = warehouseRepository
        )

        // Given
        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(originWarehouse)

        coEvery {
            warehouseRepository.findById("WH-000002")
        } returns Result.success(destinationWarehouse)

        coEvery {
            warehouseRepository.findById("WH-000003")
        } returns Result.success(newOriginWarehouse)
    }

    @Test
    fun `findAll should returns mapped routes`() = runTest {
        // Given
        coEvery {
            localDataSource.loadRoutes()
        } returns listOf(rawRoute)

        // When
        val result = repository.findAll()

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).hasSize(1)
        assertThat(result.getOrThrow().first().id)
            .isEqualTo(rawRoute.routeId)
        assertThat(result.getOrThrow().first().originWarehouse.id)
            .isEqualTo(originWarehouse.id)
        assertThat(result.getOrThrow().first().destinationWarehouse.id)
            .isEqualTo(destinationWarehouse.id)

        assertThat(originWarehouse.outgoingRoutes)
            .contains(result.getOrThrow().first())
    }

    @Test
    fun `findById should returns matching route`() = runTest {
        // Given
        coEvery {
            localDataSource.loadRoutes()
        } returns listOf(rawRoute)

        // When
        val result = repository.findById(rawRoute.routeId)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()?.id)
            .isEqualTo(rawRoute.routeId)
    }

    @Test
    fun `findById should returns null when route does not exist`() = runTest {
        // Given
        coEvery {
            localDataSource.loadRoutes()
        } returns listOf(rawRoute)

        // When
        val result = repository.findById("ROUTE-999999")

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isNull()
    }

    @Test
    fun `findAll loads local routes only once while cache is loaded`() = runTest {
        // Given
        coEvery {
            localDataSource.loadRoutes()
        } returns listOf(rawRoute)

        // When
        repository.findAll()
        repository.findAll()

        // Then
        coVerify(exactly = 1) {
            localDataSource.loadRoutes()
        }
    }

    @Test
    fun `add creates route remotely and updates local data and relations`() = runTest {
        // Given
        val route = createRoute()

        coEvery {
            localDataSource.loadRoutes()
        } returns emptyList()

        coEvery {
            remoteDataSource.createRoute(any())
        } returns routeDto

        coEvery {
            localDataSource.addRoute(any())
        } returns Unit

        // When
        val result = repository.add(route)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().id).isEqualTo(routeDto.id)
        assertThat(originWarehouse.outgoingRoutes)
            .contains(result.getOrThrow())

        coVerify(exactly = 1) {
            remoteDataSource.createRoute(any())
            localDataSource.addRoute(any())
        }
    }

    @Test
    fun `update changes route and moves relation to new origin warehouse`() = runTest {
        // Given
        val updatedDto = routeDto.copy(
            originWarehouseId = newOriginWarehouse.id,
            distanceKm = 40.0,
            typicalDelayMin = 20
        )

        coEvery {
            localDataSource.loadRoutes()
        } returns listOf(rawRoute)

        coEvery {
            remoteDataSource.updateRoute(
                id = rawRoute.routeId,
                routeRequest = any()
            )
        } returns updatedDto

        coEvery {
            localDataSource.updateRoute(any())
        } returns Unit

        // When
        val result = repository.update(
            routeId = rawRoute.routeId,
            updateModel = RouteUpdateModel(
                originWarehouseId = newOriginWarehouse.id,
                distanceKm = 40.0,
                typicalDelayMin = 20
            )
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().distanceKm).isEqualTo(40.0)
        assertThat(result.getOrThrow().typicalDelayMin).isEqualTo(20)
        assertThat(result.getOrThrow().originWarehouse.id)
            .isEqualTo(newOriginWarehouse.id)

        assertThat(originWarehouse.outgoingRoutes)
            .doesNotContain(result.getOrThrow())
        assertThat(newOriginWarehouse.outgoingRoutes)
            .contains(result.getOrThrow())

        coVerify(exactly = 1) {
            localDataSource.updateRoute(any())
        }
    }

    @Test
    fun `delete removes route remotely locally and from origin warehouse`() = runTest {
        // Given
        coEvery {
            localDataSource.loadRoutes()
        } returns listOf(rawRoute)

        coEvery {
            remoteDataSource.deleteRoute(rawRoute.routeId)
        } returns Unit

        coEvery {
            localDataSource.deleteRoute(rawRoute.routeId)
        } returns Unit

        // When
        val result = repository.delete(rawRoute.routeId)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(originWarehouse.outgoingRoutes).isEmpty()

        coVerify(exactly = 1) {
            remoteDataSource.deleteRoute(rawRoute.routeId)
        }
        coVerify(exactly = 1) {
            localDataSource.deleteRoute(rawRoute.routeId)
        }
    }

    @Test
    fun `delete returns failure when route does not exist`() = runTest {
        // Given
        coEvery {
            localDataSource.loadRoutes()
        } returns emptyList()

        // When
        val result = repository.delete("ROUTE-999999")

        // Then
        assertThat(result.isFailure).isTrue()

        coVerify(exactly = 0) {
            remoteDataSource.deleteRoute(any())
        }
        coVerify(exactly = 0) {
            localDataSource.deleteRoute(any())
        }
    }

    @Test
    fun `refresh replaces local routes with remote routes`() = runTest {
        // Given
        coEvery {
            remoteDataSource.getAllRoutes()
        } returns listOf(routeDto)

        coEvery {
            localDataSource.replaceRoutes(any())
        } returns Unit

        // When
        val result = repository.refresh()

        // Then
        assertThat(result.isSuccess).isTrue()

        coVerify(exactly = 1) {
            localDataSource.replaceRoutes(
                match { routes ->
                    routes.size == 1 &&
                            routes.first().routeId == routeDto.id
                }
            )
        }
    }

    @Test
    fun `initializeOutgoingRoutes loads routes and their warehouse relations`() = runTest {
        // Given
        coEvery {
            localDataSource.loadRoutes()
        } returns listOf(rawRoute)

        // When
        val result = repository.initializeOutgoingRoutes()

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(originWarehouse.outgoingRoutes)
            .contains(createRoute())

        coVerify(exactly = 1) {
            localDataSource.loadRoutes()
        }
    }

    @Test
    fun `findAll returns failure when local data source fails`() = runTest {
        // Given
        coEvery {
            localDataSource.loadRoutes()
        } throws IllegalStateException("Local storage failed")

        // When
        val result = repository.findAll()

        // Then
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `findAll propagates coroutine cancellation`() = runTest {
        // Given
        coEvery {
            localDataSource.loadRoutes()
        } throws CancellationException("Test cancellation")

        // When
        val thrownException = try {
            repository.findAll()
            null
        } catch (exception: CancellationException) {
            exception
        }

        // Then
        assertThat(thrownException).isNotNull()
        assertThat(thrownException?.message)
            .isEqualTo("Test cancellation")
    }
}

