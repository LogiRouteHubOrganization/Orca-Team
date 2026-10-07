package mainTest.domainTest.usecaseTest.knapsackTest

import com.google.common.truth.Truth.assertThat
import domain.usecase.OptimizeCargoByWeightUseCase
import domain.model.Package
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class OptimizeCargoByWeightUseCaseTest {

    @Test
    fun `should return empty list when packages are empty`() {
        val useCase = OptimizeCargoByWeightUseCase()

        val result = useCase(
            packages = emptyList(),
            capacityKg = 100.0
        )

        assertThat(result).isEmpty()
    }


    @Test
    fun `should return empty list when capacity is zero`() {
        val packageItem = mockPackage(
            weight = 10.0,
            priority = "URGENT"
        )

        val useCase = OptimizeCargoByWeightUseCase()

        val result = useCase(
            packages = listOf(packageItem),
            capacityKg = 0.0
        )

        assertThat(result).isEmpty()
    }

    private fun mockPackage(
        weight: Double,
        priority: String
    ): Package {
        return mockk {
            every { weightByKg } returns weight
            every { this@mockk.priority } returns priority
        }
    }
}