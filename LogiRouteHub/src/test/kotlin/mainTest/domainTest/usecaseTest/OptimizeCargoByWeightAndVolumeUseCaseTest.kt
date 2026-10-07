package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.usecase.OptimizeCargoByWeightAndVolumeUseCase
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
}