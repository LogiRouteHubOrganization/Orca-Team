package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth
import domain.model.Package
import domain.model.Vehicle
import domain.usecase.ValidateVehicleCapacityUseCase
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class ValidateVehicleCapacityUseCaseTest {

    @Test
    fun `should return true when total weight fits within vehicle capacity`() {

        // Given
        val validateVehicleCapacityUseCase =
            ValidateVehicleCapacityUseCase()

        val vehicle = mockk<Vehicle>()
        val package1 = mockk<Package>()
        val package2 = mockk<Package>()

        every { vehicle.calculateTotalLoadedWeightKg()
        } returns 20.0

        every { vehicle.maxCapacityKg } returns 100.0

        every { package1.weightByKg } returns 30.0

        every { package2.weightByKg } returns 40.0

        // When
        val result = validateVehicleCapacityUseCase(
            vehicle = vehicle,
            packages = listOf(package1, package2)
        )

        // Then
        Truth.assertThat(result).isTrue()
    }

    @Test
    fun `should return true when total weight exactly equals vehicle capacity`() {

        // Given
        val validateVehicleCapacityUseCase =
            ValidateVehicleCapacityUseCase()

        val vehicle = mockk<Vehicle>()
        val package1 = mockk<Package>()

        every { vehicle.calculateTotalLoadedWeightKg()
        } returns 20.0

        every { vehicle.maxCapacityKg } returns 100.0

        every { package1.weightByKg } returns 80.0

        // When
        val result = validateVehicleCapacityUseCase(
            vehicle = vehicle,
            packages = listOf(package1)
        )

        // Then
        Truth.assertThat(result).isTrue()
    }

    @Test
    fun `should return false when total weight exceeds vehicle capacity`() {

        // Given
        val validateVehicleCapacityUseCase =
            ValidateVehicleCapacityUseCase()

        val vehicle = mockk<Vehicle>()
        val package1 = mockk<Package>()

        every { vehicle.calculateTotalLoadedWeightKg()
        } returns 20.0

        every { vehicle.maxCapacityKg } returns 100.0

        every { package1.weightByKg } returns 81.0

        // When
        val result = validateVehicleCapacityUseCase(
            vehicle = vehicle,
            packages = listOf(package1)
        )

        // Then
        Truth.assertThat(result).isFalse()
    }

    @Test
    fun `should return true when there are no packages and current load is within capacity`() {

        // Given
        val validateVehicleCapacityUseCase =
            ValidateVehicleCapacityUseCase()

        val vehicle = mockk<Vehicle>()

        every { vehicle.calculateTotalLoadedWeightKg()
        } returns 60.0

        every { vehicle.maxCapacityKg } returns 100.0

        // When
        val result = validateVehicleCapacityUseCase(
            vehicle = vehicle,
            packages = emptyList()
        )
        // Then
        Truth.assertThat(result).isTrue()
    }

    @Test
    fun `should return true when vehicle is empty and packages fit within capacity`() {

        // Given
        val validateVehicleCapacityUseCase =
            ValidateVehicleCapacityUseCase()

        val vehicle = mockk<Vehicle>()
        val package1 = mockk<Package>()
        val package2 = mockk<Package>()

        every { vehicle.calculateTotalLoadedWeightKg()
        } returns 0.0

        every { vehicle.maxCapacityKg } returns 100.0

        every { package1.weightByKg } returns 30.0

        every { package2.weightByKg } returns 40.0

        // When
        val result = validateVehicleCapacityUseCase(
            vehicle = vehicle,
            packages = listOf(package1, package2)
        )

        // Then
        Truth.assertThat(result).isTrue()
    }

    @Test
    fun `should return false when combined package weight exceeds vehicle capacity`() {

        // Given
        val validateVehicleCapacityUseCase =
            ValidateVehicleCapacityUseCase()

        val vehicle = mockk<Vehicle>()

        val package1 = mockk<Package>()
        val package2 = mockk<Package>()
        val package3 = mockk<Package>()

        every { vehicle.calculateTotalLoadedWeightKg()
        } returns 20.0

        every { vehicle.maxCapacityKg } returns 100.0

        every { package1.weightByKg } returns 30.0

        every { package2.weightByKg } returns 40.0

        every { package3.weightByKg } returns 20.0

        // When
        val result = validateVehicleCapacityUseCase(
            vehicle = vehicle,
            packages = listOf(package1, package2, package3))

        // Then
        Truth.assertThat(result).isFalse()
    }
}
