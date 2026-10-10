package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Package
import domain.usecase.OptimizeCargoByWeightAndVolumeUseCase
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class OptimizeCargoByWeightAndVolumeUseCaseTest {

    private val useCase = OptimizeCargoByWeightAndVolumeUseCase()

    @Test
    fun `should return an empty list when packages are empty`() {
        val result = useCase(
            packages = emptyList(),
            capacityKg = 100.0,
            capacityVolumeM3 = 10.0
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun `should return an empty list when volume capacity is zero`() {
        val packageItem = mockPackage(
            weight = 10.0,
            volume = 2.0,
            priority = "URGENT"
        )

        val result = useCase(
            packages = listOf(packageItem),
            capacityKg = 100.0,
            capacityVolumeM3 = 0.0
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun `should select the package when it fits both weight and volume capacity`() {
        val packageItem = mockPackage(
            weight = 5.0,
            volume = 2.0,
            priority = "URGENT"
        )

        val result = useCase(
            packages = listOf(packageItem),
            capacityKg = 10.0,
            capacityVolumeM3 = 5.0
        )

        assertThat(result).containsExactly(packageItem)
    }

    @Test
    fun `should not select the package when it exceeds volume capacity`() {
        val packageItem = mockPackage(
            weight = 5.0,
            volume = 8.0,
            priority = "URGENT"
        )

        val result = useCase(
            packages = listOf(packageItem),
            capacityKg = 10.0,
            capacityVolumeM3 = 5.0
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun `should not select the package when it exceeds weight capacity`() {
        val packageItem = mockPackage(
            weight = 15.0,
            volume = 2.0,
            priority = "URGENT"
        )

        val result = useCase(
            packages = listOf(packageItem),
            capacityKg = 10.0,
            capacityVolumeM3 = 5.0
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun `should select the combination with the maximum total priority under both constraints`() {
        val urgentPackage = mockPackage(
            weight = 6.0,
            volume = 3.0,
            priority = "URGENT"
        )

        val standardPackage = mockPackage(
            weight = 4.0,
            volume = 2.0,
            priority = "STANDARD"
        )

        val lowPackage = mockPackage(
            weight = 10.0,
            volume = 5.0,
            priority = "LOW"
        )

        val result = useCase(
            packages = listOf(
                urgentPackage,
                standardPackage,
                lowPackage
            ),
            capacityKg = 10.0,
            capacityVolumeM3 = 5.0
        )

        assertThat(result).containsExactly(
            urgentPackage,
            standardPackage
        ).inOrder()
    }

    @Test
    fun `should respect volume capacity when weight alone would allow the combination`() {
        val bulkyPackage = mockPackage(
            weight = 10.0,
            volume = 4.0,
            priority = "URGENT"
        )

        val densePackage = mockPackage(
            weight = 40.0,
            volume = 2.0,
            priority = "STANDARD"
        )

        val smallPackage = mockPackage(
            weight = 5.0,
            volume = 1.0,
            priority = "STANDARD"
        )

        val result = useCase(
            packages = listOf(
                bulkyPackage,
                densePackage,
                smallPackage
            ),
            capacityKg = 50.0,
            capacityVolumeM3 = 5.0
        )

        assertThat(result).containsExactly(
            bulkyPackage,
            smallPackage
        ).inOrder()
    }

    @Test
    fun `should select packages that exactly fill both weight and volume capacities`() {
        val firstPackage = mockPackage(
            weight = 6.0,
            volume = 3.0,
            priority = "URGENT"
        )

        val secondPackage = mockPackage(
            weight = 4.0,
            volume = 2.0,
            priority = "STANDARD"
        )

        val result = useCase(
            packages = listOf(
                firstPackage,
                secondPackage
            ),
            capacityKg = 10.0,
            capacityVolumeM3 = 5.0
        )

        assertThat(result).containsExactly(
            firstPackage,
            secondPackage
        ).inOrder()
    }

    private fun mockPackage(
        weight: Double,
        volume: Double,
        priority: String
    ): Package {
        val packageMock = mockk<Package>()

        every { packageMock.weightByKg } returns weight
        every { packageMock.volumeM3 } returns volume
        every { packageMock.priority } returns priority

        return packageMock
    }
}
