package mainTest.domainTest.validatorTest.routeTest

import domain.validator.route.UpdateRouteValidator



import com.google.common.truth.Truth.assertThat
import domain.model.updateinput.RouteUpdateModel
import domain.validator.ErrorValidator
import domain.validator.FieldValidator
import domain.validator.ResultValidator
import org.junit.jupiter.api.Test

class UpdateRouteValidatorTest {

    private val validator = UpdateRouteValidator()

    @Test
    fun `returns valid when update data is correct`() {

        // Given
        val updateModel = RouteUpdateModel(
            distanceKm = 20.0
        )

        // When
        val result = validator(
            routeId = "RT-000001",
            updateModel = updateModel
        )

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `returns invalid when route id has wrong prefix`() {

        // Given
        val updateModel = RouteUpdateModel(
            distanceKm = 20.0
        )

        // When
        val result = validator(
            routeId = "WRONG-001",
            updateModel = updateModel
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).contains(
            FieldValidator(
                field = "routeId",
                error = ErrorValidator.InvalidIdPrefix(
                    allowedPrefixes = setOf("RT-")
                )
            )
        )
    }

    @Test
    fun `returns invalid when origin warehouse id has wrong prefix`() {

        // Given
        val updateModel = RouteUpdateModel(
            originWarehouseId = "WRONG-001"
        )

        // When
        val result = validator(
            routeId = "RT-000001",
            updateModel = updateModel
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).contains(
            FieldValidator(
                field = "originWarehouseId",
                error = ErrorValidator.InvalidIdPrefix(
                    allowedPrefixes = setOf("WH-")
                )
            )
        )
    }

    @Test
    fun `returns invalid when destination warehouse id has wrong prefix`() {

        // Given
        val updateModel = RouteUpdateModel(
            destinationWarehouseId = "WRONG-002"
        )

        // When
        val result = validator(
            routeId = "RT-000001",
            updateModel = updateModel
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).contains(
            FieldValidator(
                field = "destinationWarehouseId",
                error = ErrorValidator.InvalidIdPrefix(
                    allowedPrefixes = setOf("WH-")
                )
            )
        )
    }

    @Test
    fun `returns invalid when distance is zero`() {

        // Given
        val updateModel = RouteUpdateModel(
            distanceKm = 0.0
        )

        // When
        val result = validator(
            routeId = "RT-000001",
            updateModel = updateModel
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

        // Given
        val updateModel = RouteUpdateModel(
            distanceKm = -5.0
        )

        // When
        val result = validator(
            routeId = "RT-000001",
            updateModel = updateModel
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

        // Given
        val updateModel = RouteUpdateModel(
            typicalDelayMin = 0
        )

        // When
        val result = validator(
            routeId = "RT-000001",
            updateModel = updateModel
        )

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `returns invalid when typical delay is negative`() {

        // Given
        val updateModel = RouteUpdateModel(
            typicalDelayMin = -1
        )

        // When
        val result = validator(
            routeId = "RT-000001",
            updateModel = updateModel
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

    @Test
    fun `returns invalid when update is empty`() {

        // Given
        val updateModel = RouteUpdateModel()

        // When
        val result = validator(
            routeId = "RT-000001",
            updateModel = updateModel
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).contains(
            FieldValidator(
                field = "update",
                error = ErrorValidator.EmptyUpdate
            )
        )
    }
}
