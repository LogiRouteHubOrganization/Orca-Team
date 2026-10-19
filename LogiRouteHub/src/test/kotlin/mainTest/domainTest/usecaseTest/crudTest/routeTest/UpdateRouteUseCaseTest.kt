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

    private val routeRepository = mockk<RouteRepository>()
    private val routeValidator = mockk<UpdateRouteValidator>()
    private val useCase = UpdateRouteUseCase(routeRepository, routeValidator)

    @Test
    fun `returns updated route when validation and repository update succeed`() = runTest {
        val routeId = "RT-000001"
        val updateModel = RouteUpdateModel(distanceKm = 20.0)
        val route = createTestRoute()

        every { routeValidator(routeId, updateModel) } returns ResultValidator.Valid
        coEvery { routeRepository.update(routeId, updateModel) } returns Result.success(route)

        val result = useCase(routeId, updateModel)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(route)

        coVerify(exactly = 1) { routeValidator(routeId, updateModel) }
        coVerify(exactly = 1) { routeRepository.update(routeId, updateModel) }
    }

    @Test
    fun `returns validation failure when validation fails`() = runTest {
        val routeId = "WRONG-001"
        val updateModel = RouteUpdateModel(distanceKm = 20.0)

        every { routeValidator(routeId, updateModel) } returns ResultValidator.Invalid(emptyList())

        val result = useCase(routeId, updateModel)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(EntityValidationException::class.java)

        coVerify(exactly = 1) { routeValidator(routeId, updateModel) }
        coVerify(exactly = 0) { routeRepository.update(any(), any()) }
    }

    @Test
    fun `returns failure when repository update fails`() = runTest {
        val routeId = "RT-000001"
        val updateModel = RouteUpdateModel(distanceKm = 20.0)
        val exception = RuntimeException("Failed to update route")

        every { routeValidator(routeId, updateModel) } returns ResultValidator.Valid
        coEvery { routeRepository.update(routeId, updateModel) } returns Result.failure(exception)

        val result = useCase(routeId, updateModel)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) { routeValidator(routeId, updateModel) }
        coVerify(exactly = 1) { routeRepository.update(routeId, updateModel) }
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
