package mainTest.domainTest.validatorTest.routeTest

import com.google.common.truth.Truth.assertThat
import domain.model.Warehouse
import domain.validator.ErrorValidator
import domain.validator.FieldValidator
import domain.validator.ResultValidator
import domain.validator.route.CreateRouteValidator
import org.junit.jupiter.api.Test

class CreateRouteValidatorTest {

    private val validator = CreateRouteValidator()

    private val origin = Warehouse(
        id = "WH-000001",
        name = "Origin",
        regionalZone = "North",
        latitude = 32.22,
        longitude = 35.26
    )

    private val destination = Warehouse(
        id = "WH-000002",
        name = "Destination",
        regionalZone = "South",
        latitude = 31.95,
        longitude = 35.91
    )

    @Test
    fun `returns valid when route data is correct`() {

        // Given
        val routeId = "RT-000001"

        // When
        val result = validator(
            id = routeId,
            originWarehouse = origin,
            destinationWarehouse = destination,
            distanceKm = 10.0,
            typicalDelayMin = 5
        )

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `returns invalid when route id has wrong prefix`() {

        // Given
        val routeId = "WRONG-001"

        // When
        val result = validator(
            id = routeId,
            originWarehouse = origin,
            destinationWarehouse = destination,
            distanceKm = 10.0,
            typicalDelayMin = 5
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).contains(
            FieldValidator(
                field = "id",
                error = ErrorValidator.InvalidIdPrefix(
                    allowedPrefixes = setOf("RT-")
                )
            )
        )
    }


    @Test
    fun `returns invalid when distance is zero`() {

        // When
        val result = validator(
            id = "RT-000001",
            originWarehouse = origin,
            destinationWarehouse = destination,
            distanceKm = 0.0,
            typicalDelayMin = 5
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).contains(
            FieldValidator(
                field = "distanceKm",
                error = ErrorValidator.MustBePositive
            )
        )
    }

    @Test
    fun `returns invalid when distance is negative`() {

        // When
        val result = validator(
            id = "RT-000001",
            originWarehouse = origin,
            destinationWarehouse = destination,
            distanceKm = -5.0,
            typicalDelayMin = 5
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).contains(
            FieldValidator(
                field = "distanceKm",
                error = ErrorValidator.MustBePositive
            )
        )
    }

    @Test
    fun `returns valid when typical delay is zero`() {

        // When
        val result = validator(
            id = "RT-000001",
            originWarehouse = origin,
            destinationWarehouse = destination,
            distanceKm = 10.0,
            typicalDelayMin = 0
        )

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `returns invalid when typical delay is negative`() {

        // When
        val result = validator(
            id = "RT-000001",
            originWarehouse = origin,
            destinationWarehouse = destination,
            distanceKm = 10.0,
            typicalDelayMin = -1
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).contains(
            FieldValidator(
                field = "typicalDelayMin",
                error = ErrorValidator.MustBeNonNegative
            )
        )
    }
}

