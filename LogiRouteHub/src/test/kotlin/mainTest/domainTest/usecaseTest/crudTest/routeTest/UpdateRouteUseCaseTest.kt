package mainTest.domainTest.usecaseTest.crudTest.routeTest


import com.google.common.truth.Truth.assertThat
import domain.exception.EntityValidationException
import domain.model.Route
import domain.model.Warehouse
import domain.model.updateinput.RouteUpdateModel
import domain.repository.RouteRepository
import domain.usecase.crud.route.UpdateRouteUseCase
import domain.validator.ResultValidator
import domain.validator.route.UpdateRouteValidator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateRouteUseCaseTest {

    @Test
    fun `returns updated route when validation and repository update succeed`() = runTest {

        // Given
        val routeRepository = mockk<RouteRepository>()
        val routeValidator = mockk<UpdateRouteValidator>()

        val routeId = "RT-000001"

        val updateModel = RouteUpdateModel(
            distanceKm = 20.0
        )

        val route = createTestRoute()

        every {
            routeValidator(
                routeId = routeId,
                updateModel = updateModel
            )
        } returns ResultValidator.Valid

        coEvery {
            routeRepository.update(
                routeId = routeId,
                updateModel = updateModel
            )
        } returns Result.success(route)

        val useCase = UpdateRouteUseCase(
            routeRepository,
            routeValidator
        )

        // When
        val result = useCase(
            routeId,
            updateModel
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(route)

        coVerify(exactly = 1) {
            routeValidator(
                routeId = routeId,
                updateModel = updateModel
            )
        }

        coVerify(exactly = 1) {
            routeRepository.update(
                routeId = routeId,
                updateModel = updateModel
            )
        }
    }

    @Test
    fun `returns validation failure when validation fails`() = runTest {

        // Given
        val routeRepository = mockk<RouteRepository>()
        val routeValidator = mockk<UpdateRouteValidator>()

        val routeId = "WRONG-001"

        val updateModel = RouteUpdateModel(
            distanceKm = 20.0
        )

        every {
            routeValidator(
                routeId = routeId,
                updateModel = updateModel
            )
        } returns ResultValidator.Invalid(emptyList())

        val useCase = UpdateRouteUseCase(
            routeRepository,
            routeValidator
        )

        // When
        val result = useCase(
            routeId,
            updateModel
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(EntityValidationException::class.java)

        coVerify(exactly = 1) {
            routeValidator(
                routeId = routeId,
                updateModel = updateModel
            )
        }

        coVerify(exactly = 0) {
            routeRepository.update(
                routeId = routeId,
                updateModel = updateModel
            )
        }
    }

    @Test
    fun `returns failure when repository update fails`() = runTest {

        // Given
        val routeRepository = mockk<RouteRepository>()
        val routeValidator = mockk<UpdateRouteValidator>()

        val routeId = "RT-000001"

        val updateModel = RouteUpdateModel(
            distanceKm = 20.0
        )

        val exception = RuntimeException("Failed to update route")

        every {
            routeValidator(
                routeId = routeId,
                updateModel = updateModel
            )
        } returns ResultValidator.Valid

        coEvery {
            routeRepository.update(
                routeId = routeId,
                updateModel = updateModel
            )
        } returns Result.failure(exception)
        val useCase = UpdateRouteUseCase(
            routeRepository,
            routeValidator
        )

        // When
        val result = useCase(
            routeId,
            updateModel
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) {
            routeValidator(
                routeId = routeId,
                updateModel = updateModel
            )
        }

        coVerify(exactly = 1) {
            routeRepository.update(
                routeId = routeId,
                updateModel = updateModel
            )
        }
    }

    private fun createWarehouse(id: String): Warehouse {
        return Warehouse(
            id = id,
            name = "Test Warehouse",
            regionalZone = "NORTH",
            latitude = 31.5,
            longitude = 34.5
        )
    }

    private fun createTestRoute(): Route {
        return Route(
            id = "RT-000001",
            originWarehouse = createWarehouse("WH-000001"),
            destinationWarehouse = createWarehouse("WH-000002"),
            distanceKm = 20.0,
            typicalDelayMin = 5
        )
    }
}
