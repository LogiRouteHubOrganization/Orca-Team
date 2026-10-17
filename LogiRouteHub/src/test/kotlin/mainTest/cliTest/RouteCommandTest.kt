package mainTest.cliTest

import domain.model.Warehouse
import domain.usecase.FindOptimalPathUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import dispatcher.AppDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import presentation.cli.RouteCommand

@OptIn(ExperimentalCoroutinesApi::class)
class RouteCommandTest {

    private val findOptimalPathUseCase = mockk<FindOptimalPathUseCase>()

    private fun createCommand(
        inputs: List<String?>
    ): Pair<RouteCommand, FakeConsoleIO> {
        val console = FakeConsoleIO(inputs)

        val command = RouteCommand(
            findOptimalPathUseCase = findOptimalPathUseCase,
            console = console,
            dispatchers = AppDispatchers(
                io = UnconfinedTestDispatcher(),
                computation = UnconfinedTestDispatcher()
            )
        )

        return command to console
    }

    @Test
    fun `should display the optimal path when route calculation succeeds`() = runTest {
        // Given
        val origin = mockk<Warehouse>()
        val destination = mockk<Warehouse>()

        every { origin.id } returns "WH-000001"
        every { destination.id } returns "WH-000002"

        coEvery {
            findOptimalPathUseCase(
                startWarehouseId = "WH-000001",
                destinationWarehouseId = "WH-000002"
            )
        } returns Result.success(listOf(origin, destination))

        val (command, console) = createCommand(
            listOf("WH-000001", "WH-000002")
        )

        // When
        command.execute()

        // Then
        assertTrue(
            console.outputText().contains(
                "Optimal Path: WH-000001 -> WH-000002"
            )
        )

        coVerify(exactly = 1) {
            findOptimalPathUseCase(
                startWarehouseId = "WH-000001",
                destinationWarehouseId = "WH-000002"
            )
        }
    }

    @Test
    fun `should display an error when route calculation fails`() = runTest {
        // Given
        coEvery {
            findOptimalPathUseCase(
                startWarehouseId = "WH-000001",
                destinationWarehouseId = "WH-000002"
            )
        } returns Result.failure(IllegalStateException("No route found"))

        val (command, console) = createCommand(
            listOf("WH-000001", "WH-000002")
        )

        // When
        command.execute()

        // Then
        assertTrue(
            console.outputText().contains("Route failed: No route found")
        )
    }

    @Test
    fun `should not calculate route when origin input is missing`() = runTest {
        // Given
        val (command, _) = createCommand(listOf(null))

        // When
        command.execute()

        // Then
        coVerify(exactly = 0) {
            findOptimalPathUseCase(any(), any())
        }
    }
}

