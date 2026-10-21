
package data.remote.ratelimit

import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.delay
import kotlin.math.ceil

class RateLimiter(
    private val maxBucketCapacity: Int,
    private val refillRatePerSecond: Double,
    private val nanoTimeSource: NanoTimeSource = SystemNanoTimeSource
) {

    private data class BucketState(
        val tokens: Double,
        val lastRefillNanos: Long
    )

    private val state: AtomicReference<BucketState>

    init {
        require(maxBucketCapacity > 0) {
            "Bucket capacity must be greater than zero."
        }

        require(
            refillRatePerSecond.isFinite() &&
                    refillRatePerSecond > ZERO_TOKENS
        ) {
            "Refill rate must be finite and greater than zero."
        }

        state = AtomicReference(
            BucketState(
                tokens = maxBucketCapacity.toDouble(),
                lastRefillNanos = nanoTimeSource.nanoTime()
            )
        )
    }

    fun tryAcquire(): Boolean {
        while (true) {
            val current = state.get()
            val now = nanoTimeSource.nanoTime()

            val elapsedNanos =
                (now - current.lastRefillNanos)
                    .coerceAtLeast(ZERO_NANOS)

            val refilledTokens = (
                    current.tokens +
                            elapsedNanos.toDouble() / NANOS_PER_SECOND *
                            refillRatePerSecond
                    ).coerceAtMost(maxBucketCapacity.toDouble())

            val availableState = BucketState(
                tokens = refilledTokens,
                lastRefillNanos = now
            )

            if (refilledTokens < ONE_TOKEN) {
                if (state.compareAndSet(current, availableState)) {
                    return false
                }

                continue
            }

            val updatedState = availableState.copy(
                tokens = refilledTokens - ONE_TOKEN
            )

            if (state.compareAndSet(current, updatedState)) {
                return true
            }
        }
    }

    suspend fun acquire() {
        while (!tryAcquire()) {
            delay(calculateWaitMillis())
        }
    }

    private fun calculateWaitMillis(): Long {
        val current = state.get()
        val now = nanoTimeSource.nanoTime()

        val elapsedNanos =
            (now - current.lastRefillNanos)
                .coerceAtLeast(ZERO_NANOS)

        val refilledTokens = (
                current.tokens +
                        elapsedNanos.toDouble() / NANOS_PER_SECOND *
                        refillRatePerSecond
                ).coerceAtMost(maxBucketCapacity.toDouble())

        val missingTokens =
            (ONE_TOKEN - refilledTokens)
                .coerceAtLeast(ZERO_TOKENS)

        val waitNanos =
            missingTokens / refillRatePerSecond *
                    NANOS_PER_SECOND

        return ceil(waitNanos / NANOS_PER_MILLISECOND)
            .toLong()
            .coerceAtLeast(MIN_WAIT_MILLIS)
    }

    private companion object {
        const val ZERO_NANOS = 0L
        const val ZERO_TOKENS = 0.0
        const val ONE_TOKEN = 1.0
        const val MIN_WAIT_MILLIS = 1L

        const val NANOS_PER_SECOND = 1_000_000_000.0
        const val NANOS_PER_MILLISECOND = 1_000_000.0
    }
}