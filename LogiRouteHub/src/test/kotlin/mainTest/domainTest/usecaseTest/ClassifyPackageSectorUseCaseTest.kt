
package mainTest.domainTest.usecaseTest

import domain.algorithm.knn.CargoFeatures
import domain.algorithm.knn.CargoTrainingRecord
import domain.repository.PackageRepository
import domain.usecase.ClassifyPackageSectorUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import domain.algorithm.knn.KNNClassifier
import io.mockk.every
import io.mockk.coVerify
import org.junit.jupiter.api.Assertions.assertEquals
import domain.model.Package
import domain.model.Warehouse
import io.mockk.verify

class ClassifyPackageSectorUseCaseTest {

    private val packageRepository = mockk<PackageRepository>()
    private val knnClassifier = mockk<KNNClassifier>()

    private val useCase = ClassifyPackageSectorUseCase(
        packageRepository = packageRepository,
        knnClassifier = knnClassifier
    )
    private val cargoToClassify = CargoFeatures(
        latitude = 31.5,
        longitude = 34.5,
        weightKg = 10.0
    )

    @Test
    fun `should return failure when repository fails`() = runBlocking {
        // Given
        coEvery { packageRepository.findAll() } returns
                Result.failure(Exception("Database error"))

        // When
        val result = useCase(cargoToClassify)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `should return failure when training data is empty`() = runBlocking {
        // Given
        coEvery { packageRepository.findAll() } returns
                Result.success(emptyList())

        val useCaseWithRealClassifier = ClassifyPackageSectorUseCase(
            packageRepository = packageRepository,
            knnClassifier = KNNClassifier()
        )

        // When
        val result = useCaseWithRealClassifier(cargoToClassify)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `should return predicted sector when classification succeeds`() = runBlocking {
        // Given
        coEvery { packageRepository.findAll() } returns
                Result.success(emptyList())

        every {
            knnClassifier.classify(any(), any())
        } returns "NORTH"

        // When
        val result = useCase(cargoToClassify)

        // Then
        assertEquals("NORTH", result.getOrNull())
    }

    @Test
    fun `should pass cargo features to classifier`() = runBlocking {
        // Given
        coEvery { packageRepository.findAll() } returns
                Result.success(emptyList())

        every {
            knnClassifier.classify(any(), any())
        } returns "NORTH"

        // When
        useCase(cargoToClassify)

        // Then
        io.mockk.verify(exactly = 1) {
            knnClassifier.classify(cargoToClassify = cargoToClassify, trainingRecords = any())
        }
    }

    @Test
    fun `should retrieve training packages from repository`() = runBlocking {
        // Given
        coEvery { packageRepository.findAll() } returns
                Result.success(emptyList())

        every {
            knnClassifier.classify(any(), any())
        } returns "NORTH"

        // When
        useCase(cargoToClassify)

        // Then
        coVerify(exactly = 1) { packageRepository.findAll() }
    }


    @Test
    fun `should map package data to training records`() = runBlocking {
        // Given
        val destinationWarehouse = mockk<Warehouse>()
        every { destinationWarehouse.latitude } returns 31.5
        every { destinationWarehouse.longitude } returns 34.5
        every { destinationWarehouse.regionalZone } returns "NORTH"

        val logisticsPackage = mockk<Package>()
        every { logisticsPackage.destinationWarehouse } returns destinationWarehouse
        every { logisticsPackage.weightByKg } returns 10.0

        coEvery { packageRepository.findAll() } returns
                Result.success(listOf(logisticsPackage))

        every {
            knnClassifier.classify(any(), any())
        } returns "NORTH"

        val expectedTrainingRecords = listOf(
            CargoTrainingRecord(
                features = CargoFeatures(latitude = 31.5, longitude = 34.5, weightKg = 10.0),
                sector = "NORTH"
            )
        )

        // When
        useCase(cargoToClassify)

        // Then
        verify(exactly = 1) {
            knnClassifier.classify(cargoToClassify = cargoToClassify, trainingRecords = expectedTrainingRecords)
        }
    }

    @Test
    fun `should return failure when classifier throws exception`() = runBlocking {
        // Given
        coEvery { packageRepository.findAll() } returns
                Result.success(emptyList())

        every {
            knnClassifier.classify(any(), any())
        } throws IllegalArgumentException("Invalid training data")

        // When
        val result = useCase(cargoToClassify)

        // Then
        assertTrue(result.isFailure)
    }

}
