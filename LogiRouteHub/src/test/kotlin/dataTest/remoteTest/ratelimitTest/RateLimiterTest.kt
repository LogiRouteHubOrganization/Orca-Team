package dataTest.remoteTest.ratelimitTest

import data.remote.ratelimit.NanoTimeSource
import data.remote.ratelimit.RateLimiter
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RateLimiterTest {

    private class FakeNanoTimeSource(
        var currentTimeNanos: Long = 0L
    ) : NanoTimeSource {

        override fun nanoTime(): Long = currentTimeNanos

        fun advanceBy(nanos: Long) {
            require(nanos >= 0L)
            currentTimeNanos += nanos
        }
    }

    @Test
    fun `should allow acquiring a token when bucket is full`() {
        val rateLimiter = createRateLimiter()

        assertTrue(rateLimiter.tryAcquire())
    }

    @Test
    fun `should reject acquiring a token when bucket is empty`() {
        val rateLimiter = createRateLimiter()

        repeat(2) {
            rateLimiter.tryAcquire()
        }

        assertFalse(rateLimiter.tryAcquire())
    }

    @Test
    fun `should refill a token after sufficient elapsed time`() {
        val clock = FakeNanoTimeSource()
        val rateLimiter = createRateLimiter(clock)

        repeat(2) {
            rateLimiter.tryAcquire()
        }

        clock.advanceBy(200_000_000L)

        assertTrue(rateLimiter.tryAcquire())
    }

    @Test
    fun `should not exceed maximum bucket capacity after elapsed time`() {
        val clock = FakeNanoTimeSource()
        val rateLimiter = createRateLimiter(clock)

        clock.advanceBy(10_000_000_000L)

        repeat(2) {
            rateLimiter.tryAcquire()
        }

        assertFalse(rateLimiter.tryAcquire())
    }

    private fun createRateLimiter(
        clock: FakeNanoTimeSource = FakeNanoTimeSource()
    ) = RateLimiter(
        maxBucketCapacity = 2,
        refillRatePerSecond = 5.0,
        nanoTimeSource = clock
    )
}
