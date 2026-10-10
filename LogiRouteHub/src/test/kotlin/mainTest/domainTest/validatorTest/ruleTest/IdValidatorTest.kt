package mainTest.domainTest.validatorTest.ruleTest

import com.google.common.truth.Truth.assertThat
import domain.validator.ErrorValidator
import domain.validator.FieldValidator
import domain.validator.ResultValidator
import domain.validator.rule.IdValidator
import org.junit.jupiter.api.Test

class IdValidatorTest {

    @Test
    fun `returns valid when id has allowed prefix and six digits`() {
        // Given
        val validator = IdValidator(
            field = "id",
            allowedPrefixes = setOf("PKG-")
        )

        val id = "PKG-000001"

        // When
        val result = validator.validate(id)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `returns blank id error when id is blank`() {
        // Given
        val validator = IdValidator(
            field = "id",
            allowedPrefixes = setOf("PKG-")
        )

        val id = ""

        // When
        val result = validator.validate(id)

        // Then
        val expected = ResultValidator.Invalid(
            listOf(
                FieldValidator(
                    field = "id",
                    error = ErrorValidator.BlankId
                )
            )
        )

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `returns blank id error when id is null`() {
        // Given
        val validator = IdValidator(
            field = "id",
            allowedPrefixes = setOf("PKG-")
        )

        val id: String? = null

        // When
        val result = validator.validate(id)

        // Then
        val expected = ResultValidator.Invalid(
            listOf(
                FieldValidator(
                    field = "id",
                    error = ErrorValidator.BlankId
                )
            )
        )

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `returns invalid prefix error when id prefix is not allowed`() {
        // Given
        val allowedPrefixes = setOf("PKG-")

        val validator = IdValidator(
            field = "id",
            allowedPrefixes = allowedPrefixes
        )

        val id = "ABC-000001"

        // When
        val result = validator.validate(id)

        // Then
        val expected = ResultValidator.Invalid(
            listOf(
                FieldValidator(
                    field = "id",
                    error = ErrorValidator.InvalidIdPrefix(
                        allowedPrefixes = allowedPrefixes
                    )
                )
            )
        )

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `returns invalid format error when numeric part has fewer than six digits`() {
        // Given
        val validator = IdValidator(
            field = "id",
            allowedPrefixes = setOf("PKG-")
        )

        val id = "PKG-12345"

        // When
        val result = validator.validate(id)

        // Then
        val expected = ResultValidator.Invalid(
            listOf(
                FieldValidator(
                    field = "id",
                    error = ErrorValidator.InvalidIdFormat
                )
            )
        )

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `returns invalid format error when numeric part has more than six digits`() {
        val validator = IdValidator(
            field = "id",
            allowedPrefixes = setOf("PKG-")
        )

        val result = validator.validate("PKG-1234567")

        val expected = ResultValidator.Invalid(
            listOf(
                FieldValidator(
                    field = "id",
                    error = ErrorValidator.InvalidIdFormat
                )
            )
        )

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `returns invalid format error when id contains non numeric characters`() {
        val validator = IdValidator(
            field = "id",
            allowedPrefixes = setOf("PKG-")
        )

        val result = validator.validate("PKG-12AB56")

        val expected = ResultValidator.Invalid(
            listOf(
                FieldValidator(
                    field = "id",
                    error = ErrorValidator.InvalidIdFormat
                )
            )
        )

        assertThat(result).isEqualTo(expected)
    }
}
