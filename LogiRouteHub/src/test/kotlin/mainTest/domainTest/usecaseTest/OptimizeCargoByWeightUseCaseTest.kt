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

    @Test
    fun `should select package when it fits within weight capacity`() {
        val packageItem = mockPackage(
            weight = 5.0,
            priority = "URGENT"
        )

        val useCase = OptimizeCargoByWeightUseCase()

        val result = useCase(
            packages = listOf(packageItem),
            capacityKg = 10.0
        )

        assertThat(result).containsExactly(packageItem)
    }

    @Test
    fun `should not select package when it exceeds weight capacity`() {
        val packageItem = mockPackage(
            weight = 15.0,
            priority = "URGENT"
        )

        val useCase = OptimizeCargoByWeightUseCase()

        val result = useCase(
            packages = listOf(packageItem),
            capacityKg = 10.0
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun `should select combination with maximum total priority`() {
        val urgentPackage = mockPackage(
            weight = 6.0,
            priority = "URGENT"
        )

        val standardPackage = mockPackage(
            weight = 4.0,
            priority = "STANDARD"
        )

        val lowPackage = mockPackage(
            weight = 10.0,
            priority = "LOW"
        )

        val useCase = OptimizeCargoByWeightUseCase()

        val result = useCase(
            packages = listOf(
                urgentPackage,
                standardPackage,
                lowPackage
            ),
            capacityKg = 10.0
        )

        assertThat(result).containsExactly(
            urgentPackage,
            standardPackage
        ).inOrder()
    }
    @Test
    fun `should select packages that exactly fill weight capacity`() {
        val firstPackage = mockPackage(
            weight = 6.0,
            priority = "URGENT"
        )

        val secondPackage = mockPackage(
            weight = 4.0,
            priority = "STANDARD"
        )

        val useCase = OptimizeCargoByWeightUseCase()

        val result = useCase(
            packages = listOf(
                firstPackage,
                secondPackage
            ),
            capacityKg = 10.0
        )

        assertThat(result).containsExactly(
            firstPackage,
            secondPackage
        ).inOrder()
    }
    @Test
    fun `should select each package at most once`() {
        val packageItem = mockPackage(
            weight = 5.0,
            priority = "URGENT"
        )

        val useCase = OptimizeCargoByWeightUseCase()

        val result = useCase(
            packages = listOf(packageItem),
            capacityKg = 10.0
        )

        assertThat(result).containsExactly(packageItem)
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