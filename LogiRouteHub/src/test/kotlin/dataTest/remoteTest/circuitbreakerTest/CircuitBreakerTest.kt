package dataTest.remoteTest.circuitbreakerTest

import com.google.common.truth.Truth.assertThat
import data.exception.remote.NetworkException
import data.remote.circuitbreaker.CallNotPermittedException
import data.remote.circuitbreaker.CircuitBreaker
import domain.exception.EntityValidationException
import domain.exception.ServerErrorException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test


class CircuitBreakerTest {

    private lateinit var circuitBreaker: CircuitBreaker

    private val failureThreshold = 3
    private val openDurationMs = 1000L

    @Before
    fun setUp() {
        circuitBreaker = CircuitBreaker(
            failureThreshold = failureThreshold,
            openDurationMs = openDurationMs
        )
    }

    @Test
    fun `given closed state, when successful request is executed, then it returns result successfully`() = runTest {
        // Given
        val expectedData = "Valid Response Data"

        // When
        val actualResult = circuitBreaker.execute {
            expectedData
        }

        // Then
        assertThat(actualResult).isEqualTo(expectedData)
    }

    @Test
    fun `given closed state, when non-network exception occurs, then it does not count towards failure threshold`() = runTest {
        // Given
        val nonNetworkException = EntityValidationException(errors = emptyList())

        // When
        repeat(failureThreshold) {
            try {
                circuitBreaker.execute { throw nonNetworkException }
            } catch (_: EntityValidationException) {
            }
        }
        val actualResult = circuitBreaker.execute { "Success After Validation Error" }

        // Then
        assertThat(actualResult).isEqualTo("Success After Validation Error")
    }

    @Test
    fun `given closed state, when network failures reach threshold, then circuit transitions to open state`() = runTest {
        // Given
        repeat(failureThreshold) {
            try {
                circuitBreaker.execute {
                    throw ServerErrorException("Internal Server Error")
                }
            } catch (_: Exception) {
            }
        }

        // When
        val caughtException = assertThrows(CallNotPermittedException::class.java) {
            runTest {
                circuitBreaker.execute { "Should not be executed" }
            }
        }

        // Then
        assertThat(caughtException).isNotNull()
    }

    @Test
    fun `given open state, when open duration expires, then circuit transitions to half-open and allows probe request`() = runTest {
        // Given
        repeat(failureThreshold) {
            try {
                circuitBreaker.execute { throw NetworkException.NoInternetException() }
            } catch (_: Exception) {}
        }

        // When
        delay(openDurationMs + 100)
        val actualResult = circuitBreaker.execute {
            "Recovered Data"
        }

        // Then
        assertThat(actualResult).isEqualTo("Recovered Data")
    }

    @Test
    fun `given half-open state, when probe request fails, then circuit transitions back to open state`() = runTest {
        // Given
        repeat(failureThreshold) {
            try {
                circuitBreaker.execute { throw ServerErrorException("Error") }
            } catch (_: Exception) {}
        }
        delay(openDurationMs + 100)

        // When
        val caughtException = assertThrows(CallNotPermittedException::class.java) {
            runTest {
                try {
                    circuitBreaker.execute {
                        throw ServerErrorException("Service Unavailable")
                    }
                } catch (_: Exception) {}

                circuitBreaker.execute { "Should be blocked again" }
            }
        }

        // Then
        assertThat(caughtException).isNotNull()
    }
}
