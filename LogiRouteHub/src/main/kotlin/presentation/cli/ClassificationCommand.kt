package presentation.cli

import dispatcher.AppDispatchers
import domain.algorithm.knn.CargoFeatures
import domain.usecase.ClassifyPackageSectorUseCase
import domain.validator.ResultValidator
import domain.validator.rule.LatitudeValidator
import domain.validator.rule.LongitudeValidator
import kotlinx.coroutines.withContext

class ClassificationCommand(
    private val classifyPackageSectorUseCase: ClassifyPackageSectorUseCase,
    private val console: ConsoleIO,
    private val dispatchers: AppDispatchers,
    private val latitudeValidator: LatitudeValidator,
    private val longitudeValidator: LongitudeValidator
) {

    suspend fun execute() {
        console.println("\n===== Package Classification =====")

        val latitude = readNumber("Enter latitude: ")
            ?: return

        if (latitudeValidator.validate(latitude) is ResultValidator.Invalid) {
            console.println("Invalid latitude. It must be between -90 and 90.")
            return
        }

        val longitude = readNumber("Enter longitude: ")
            ?: return

        if (longitudeValidator.validate(longitude) is ResultValidator.Invalid) {
            console.println("Invalid longitude. It must be between -180 and 180.")
            return
        }

        val weight = readNumber("Enter weight (kg): ")
            ?: return

        if (weight <= 0.0) {
            console.println("Invalid weight. It must be greater than zero.")
            return
        }

        val cargo = CargoFeatures(
            latitude = latitude,
            longitude = longitude,
            weightKg = weight
        )

        val result = withContext(dispatchers.computation) {
            classifyPackageSectorUseCase(cargo)
        }

        result
            .onSuccess { sector ->
                console.println("Predicted Sector: $sector")
            }
            .onFailure { error ->
                console.println("Classification failed: ${error.message}")
            }
    }

    private suspend fun readNumber(message: String): Double? {
        console.print(message)

        val number = console.readLine()?.toDoubleOrNull()

        if (number == null || !number.isFinite()) {
            console.println("Invalid number.")
            return null
        }

        return number
    }
}
