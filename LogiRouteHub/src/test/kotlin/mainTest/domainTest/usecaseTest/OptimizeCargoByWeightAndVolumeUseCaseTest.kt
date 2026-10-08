package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Package
import domain.usecase.OptimizeCargoByWeightAndVolumeUseCase
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class OptimizeCargoByWeightAndVolumeUseCaseTest {

    @Test
    fun shouldReturnEmptyListWhenPackagesAreEmpty() {
        val useCase = OptimizeCargoByWeightAndVolumeUseCase()

        val result = useCase(
            packages = emptyList(),
            capacityKg = 100.0,
            capacityVolumeM3 = 10.0
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun shouldReturnEmptyListWhenVolumeCapacityIsZero() {
        val packageItem = mockPackage(
            weight = 10.0,
            volume = 2.0,
            priority = "URGENT"
        )

        val useCase = OptimizeCargoByWeightAndVolumeUseCase()

        val result = useCase(
            packages = listOf(packageItem),
            capacityKg = 100.0,
            capacityVolumeM3 = 0.0
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun shouldSelectPackageWhenItFitsWeightAndVolumeCapacity() {
        val packageItem = mockPackage(
            weight = 5.0,
            volume = 2.0,
            priority = "URGENT"
        )

        val useCase = OptimizeCargoByWeightAndVolumeUseCase()

        val result = useCase(
            packages = listOf(packageItem),
            capacityKg = 10.0,
            capacityVolumeM3 = 5.0
        )

        assertThat(result).containsExactly(packageItem)
    }

    @Test
    fun shouldNotSelectPackageWhenItExceedsVolumeCapacity() {
        val packageItem = mockPackage(
            weight = 5.0,
            volume = 8.0,
            priority = "URGENT"
        )

        val useCase = OptimizeCargoByWeightAndVolumeUseCase()

        val result = useCase(
            packages = listOf(packageItem),
            capacityKg = 10.0,
            capacityVolumeM3 = 5.0
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun shouldNotSelectPackageWhenItExceedsWeightCapacity() {
        val packageItem = mockPackage(
            weight = 15.0,
            volume = 2.0,
            priority = "URGENT"
        )

        val useCase = OptimizeCargoByWeightAndVolumeUseCase()

        val result = useCase(
            packages = listOf(packageItem),
            capacityKg = 10.0,
            capacityVolumeM3 = 5.0
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun shouldSelectCombinationWithMaximumTotalPriorityUnderBothConstraints() {
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

        val useCase = OptimizeCargoByWeightAndVolumeUseCase()

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