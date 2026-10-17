package mainTest.presentationTest.cliTest

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import presentation.cli.ClassificationCommand
import presentation.cli.FleetTrackingCommand
import presentation.cli.InteractiveCli
import presentation.cli.RouteCommand

class InteractiveCliTest {

    private val routeCommand = mockk<RouteCommand>()
    private val classificationCommand = mockk<ClassificationCommand>()
    private val fleetTrackingCommand = mockk<FleetTrackingCommand>()

    private fun createCli(
        inputs: List<String?>
    ): Pair<InteractiveCli, FakeConsoleIO> {
        val console = FakeConsoleIO(inputs)

        return InteractiveCli(
            routeCommand = routeCommand,
            classificationCommand = classificationCommand,
            fleetTrackingCommand = fleetTrackingCommand,
            console = console
        ) to console
    }

    @Test
    fun `should exit and stop fleet tracking when user selects exit`() = runTest {
        // Given
        coEvery { fleetTrackingCommand.stop() } returns Unit

        val (cli, console) = createCli(listOf("4"))

        // When
        cli.start()

        // Then
        assertTrue(console.outputText().contains("Goodbye!"))
        coVerify(exactly = 1) { fleetTrackingCommand.stop() }
    }

    @Test
    fun `should show invalid choice when selection is not supported`() = runTest {
        // Given
        coEvery { fleetTrackingCommand.stop() } returns Unit

        val (cli, console) = createCli(listOf("9", "4"))

        // When
        cli.start()

        // Then
        assertTrue(console.outputText().contains("Invalid choice."))
        coVerify(exactly = 1) { fleetTrackingCommand.stop() }
    }

    @Test
    fun `should execute route command when user selects route calculation`() = runTest {
        // Given
        coEvery { routeCommand.execute() } returns Unit
        coEvery { fleetTrackingCommand.stop() } returns Unit

        val (cli, _) = createCli(listOf("1", "4"))

        // When
        cli.start()

        // Then
        coVerify(exactly = 1) { routeCommand.execute() }
        coVerify(exactly = 1) { fleetTrackingCommand.stop() }
    }

    @Test
    fun `should execute classification command when user selects classification`() = runTest {
        // Given
        coEvery { classificationCommand.execute() } returns Unit
        coEvery { fleetTrackingCommand.stop() } returns Unit

        val (cli, _) = createCli(listOf("2", "4"))

        // When
        cli.start()

        // Then
        coVerify(exactly = 1) { classificationCommand.execute() }
    }
}