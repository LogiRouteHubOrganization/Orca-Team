package presentation.cli

import domain.algorithm.knn.CargoFeatures
import domain.usecase.ClassifyPackageSectorUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ClassificationCommand(
    private val classifyPackageSectorUseCase: ClassifyPackageSectorUseCase
) {

    suspend fun execute() {
        println("\n===== Package Classification =====")

        val latitude = readNumber("Enter latitude: ") ?: return
        val longitude = readNumber("Enter longitude: ") ?: return
        val weight = readNumber("Enter weight (kg): ") ?: return

        val cargo = CargoFeatures(
            latitude = latitude,
            longitude = longitude,
            weightKg = weight
        )

        classifyPackageSectorUseCase(cargo)
            .onSuccess { sector ->
                println("Predicted Sector: $sector")
            }
            .onFailure { error ->
                println("Classification failed: ${error.message}")
            }
    }

    private suspend fun readNumber(message: String): Double? {
        print(message)

        val number = withContext(Dispatchers.IO) {
            readlnOrNull()
        }?.toDoubleOrNull()

        if (number == null || !number.isFinite()) {
            println("Invalid number.")
            return null
        }

        return number
    }
}