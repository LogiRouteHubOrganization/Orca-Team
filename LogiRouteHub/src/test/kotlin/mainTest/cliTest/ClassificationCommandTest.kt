package mainTest.cliTest

import domain.algorithm.knn.CargoFeatures
import domain.usecase.ClassifyPackageSectorUseCase
import domain.validator.rule.LatitudeValidator
import domain.validator.rule.LongitudeValidator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import dispatcher.AppDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import presentation.cli.ClassificationCommand

@OptIn(ExperimentalCoroutinesApi::class)
class ClassificationCommandTest {

    private val useCase = mockk<ClassifyPackageSectorUseCase>()

    private fun createCommand(
        inputs: List<String?>
    ): Pair<ClassificationCommand, FakeConsoleIO> {
        val console = FakeConsoleIO(inputs)

        val command = ClassificationCommand(
            classifyPackageSectorUseCase = useCase,
            console = console,
            dispatchers = AppDispatchers(
                io = UnconfinedTestDispatcher(),
                computation = UnconfinedTestDispatcher()
            ),
            latitudeValidator = LatitudeValidator(),
            longitudeValidator = LongitudeValidator()
        )

        return command to console
    }

    @Test
    fun `should classify cargo when all inputs are valid`() = runTest {
        // Given
        coEvery {
            useCase(any())
        } returns Result.success("NORTH")

        val (command, console) = createCommand(
            listOf("31.5", "34.5", "10")
        )

        // When
        command.execute()

        // Then
        assertTrue(console.outputText().contains("Predicted Sector: NORTH"))

        coVerify(exactly = 1) {
            useCase(
                CargoFeatures(
                    latitude = 31.5,
                    longitude = 34.5,
                    weightKg = 10.0
                )
            )
        }
    }

    @Test
    fun `should not classify when latitude is out of range`() = runTest {
        // Given
        val (command, console) = createCommand(
            listOf("91", "34.5", "10")
        )

        // When
        command.execute()

        // Then
        assertTrue(console.outputText().contains("Invalid latitude"))

        coVerify(exactly = 0) {
            useCase(any())
        }
    }

    @Test
    fun `should not classify when longitude is out of range`() = runTest {
        // Given
        val (command, console) = createCommand(
            listOf("31.5", "181", "10")
        )

        // When
        command.execute()

        // Then
        assertTrue(console.outputText().contains("Invalid longitude"))

        coVerify(exactly = 0) {
            useCase(any())
        }
    }

    @Test
    fun `should not classify when weight is zero`() = runTest {
        // Given
        val (command, console) = createCommand(
            listOf("31.5", "34.5", "0")
        )

        // When
        command.execute()

        // Then
        assertTrue(console.outputText().contains("Invalid weight"))

        coVerify(exactly = 0) {
            useCase(any())
        }
    }

    @Test
    fun `should not classify when input is not numeric`() = runTest {
        // Given
        val (command, console) = createCommand(
            listOf("north", "34.5", "10")
        )

        // When
        command.execute()

        // Then
        assertTrue(console.outputText().contains("Invalid number"))

        coVerify(exactly = 0) {
            useCase(any())
        }
    }

    @Test
    fun `should not classify when latitude is not finite`() = runTest {
        // Given
        val (command, console) = createCommand(
            listOf("NaN", "34.5", "10")
        )

        // When
        command.execute()

        // Then
        assertTrue(console.outputText().contains("Invalid number"))

        coVerify(exactly = 0) {
            useCase(any())
        }
    }
}
