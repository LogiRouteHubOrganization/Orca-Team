package mainTest.domainTest.validatorTest.ruleTest

import com.google.common.truth.Truth.assertThat
import domain.validator.ErrorValidator
import domain.validator.FieldValidator
import domain.validator.ResultValidator
import domain.validator.rule.PriorityValidator
import org.junit.jupiter.api.Test

class PriorityValidatorTest {
    @Test
    fun `returns valid when priority is STANDARD`() {
        // Given
        val validator = PriorityValidator(field = "priority")
        val priority = "STANDARD"

        // When
        val result = validator.validate(priority)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `returns valid when priority is LOW`() {
        // Given
        val validator = PriorityValidator(field = "priority")

        // When
        val result = validator.validate("LOW")

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `returns valid when priority is URGENT`() {
        // Given
        val validator = PriorityValidator(field = "priority")

        // When
        val result = validator.validate("URGENT")

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `returns blank priority error when priority is blank`() {
        // Given
        val validator = PriorityValidator(field = "priority")
        val priority = ""

        // When
        val result = validator.validate(priority)

        // Then
        val expected = ResultValidator.Invalid(
            listOf(
                FieldValidator(
                    field = "priority",
                    error = ErrorValidator.BlankPriority
                )
            )
        )

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `returns blank priority error when priority is null`() {
        // Given
        val validator = PriorityValidator(field = "priority")
        val priority: String? = null

        // When
        val result = validator.validate(priority)

        // Then
        val expected = ResultValidator.Invalid(
            listOf(
                FieldValidator(
                    field = "priority",
                    error = ErrorValidator.BlankPriority
                )
            )
        )

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `returns invalid priority error when priority is not allowed`() {
        // Given
        val allowedPriorities = setOf(
            "STANDARD",
            "LOW",
            "URGENT"
        )

        val validator = PriorityValidator(
            field = "priority",
            allowedPriorities = allowedPriorities
        )

        val priority = "HIGH"

        // When
        val result = validator.validate(priority)

        // Then
        val expected = ResultValidator.Invalid(
            listOf(
                FieldValidator(
                    field = "priority",
                    error = ErrorValidator.InvalidPriority(
                        allowedPriorities = allowedPriorities
                    )
                )
            )
        )

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `returns invalid priority error when priority uses lowercase`() {
        // Given
        val allowedPriorities = setOf(
            "STANDARD",
            "LOW",
            "URGENT"
        )

        val validator = PriorityValidator(
            field = "priority",
            allowedPriorities = allowedPriorities
        )

        // When
        val result = validator.validate("urgent")

        // Then
        val expected = ResultValidator.Invalid(
            listOf(
                FieldValidator(
                    field = "priority",
                    error = ErrorValidator.InvalidPriority(
                        allowedPriorities = allowedPriorities
                    )
                )
            )
        )

        assertThat(result).isEqualTo(expected)
    }
}
