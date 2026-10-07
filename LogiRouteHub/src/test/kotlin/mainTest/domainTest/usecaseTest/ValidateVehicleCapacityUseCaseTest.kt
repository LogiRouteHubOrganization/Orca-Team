package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Package
import domain.model.Vehicle
import domain.usecase.ValidateVehicleCapacityUseCase
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ValidateVehicleCapacityUseCaseTest {

    private lateinit var validateVehicleCapacityUseCase: ValidateVehicleCapacityUseCase
    private lateinit var vehicle: Vehicle

    @BeforeEach
    fun setUp() {
        validateVehicleCapacityUseCase = ValidateVehicleCapacityUseCase()
        vehicle = mockk()
    }


    @Test
    fun `should return true when total weight fits within vehicle capacity`() {
        stubVehicleCapacity(currentLoadKg = 20.0, maxCapacityKg = 100.0)
        val packages = listOf(createMockPackage(30.0), createMockPackage(40.0))

        val result = validateVehicleCapacityUseCase(vehicle = vehicle, packages = packages)

        assertThat(result).isTrue()
    }

    @Test
    fun `should return true when total weight exactly equals vehicle capacity`() {
        stubVehicleCapacity(currentLoadKg = 20.0, maxCapacityKg = 100.0)
        val packages = listOf(createMockPackage(80.0))

        val result = validateVehicleCapacityUseCase(vehicle = vehicle, packages = packages)

        assertThat(result).isTrue()
    }

    @Test
    fun `should return true when vehicle is empty and packages fit within capacity`() {
        stubVehicleCapacity(currentLoadKg = 0.0, maxCapacityKg = 100.0)
        val packages = listOf(createMockPackage(30.0), createMockPackage(40.0))

        val result = validateVehicleCapacityUseCase(vehicle = vehicle, packages = packages)

        assertThat(result).isTrue()
    }

    @Test
    fun `should return true when there are no packages and current load is within capacity`() {
        stubVehicleCapacity(currentLoadKg = 60.0, maxCapacityKg = 100.0)

        val result = validateVehicleCapacityUseCase(vehicle = vehicle, packages = emptyList())

        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when total weight exceeds vehicle capacity by minimal unit`() {
        stubVehicleCapacity(currentLoadKg = 20.0, maxCapacityKg = 100.0)
        val packages = listOf(createMockPackage(81.0))

        val result = validateVehicleCapacityUseCase(vehicle = vehicle, packages = packages)

        assertThat(result).isFalse()
    }

    @Test
    fun `should return false when combined package weight exceeds vehicle capacity`() {
        stubVehicleCapacity(currentLoadKg = 20.0, maxCapacityKg = 100.0)
        val packages = listOf(createMockPackage(30.0), createMockPackage(40.0), createMockPackage(31.0))

        val result = validateVehicleCapacityUseCase(vehicle = vehicle, packages = packages)

        assertThat(result).isFalse()
    }

    @Test
    fun `should return false when vehicle current load is already at maximum capacity`() {
        stubVehicleCapacity(currentLoadKg = 100.0, maxCapacityKg = 100.0)
        val packages = listOf(createMockPackage(1.0))

        val result = validateVehicleCapacityUseCase(vehicle = vehicle, packages = packages)

        assertThat(result).isFalse()
    }


    private fun stubVehicleCapacity(currentLoadKg: Double, maxCapacityKg: Double) {
        every { vehicle.calculateTotalLoadedWeightKg() } returns currentLoadKg
        every { vehicle.maxCapacityKg } returns maxCapacityKg
    }

    private fun createMockPackage(weightByKg: Double): Package {
        val mockPackage = mockk<Package>()
        every { mockPackage.weightByKg } returns weightByKg
        return mockPackage
    }
}