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
        require(refillRatePerSecond.isFinite() && refillRatePerSecond > 0.0) {
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
                (now - current.lastRefillNanos).coerceAtLeast(0L)

            val refilledTokens = (
                    current.tokens +
                            elapsedNanos.toDouble() / NANOS_PER_SECOND *
                            refillRatePerSecond
                    ).coerceAtMost(maxBucketCapacity.toDouble())

            val availableState = BucketState(
                tokens = refilledTokens,
                lastRefillNanos = now
            )

            if (refilledTokens < 1.0) {
                if (state.compareAndSet(current, availableState)) {
                    return false
                }

                continue
            }

            val updatedState = availableState.copy(
                tokens = refilledTokens - 1.0
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
            (now - current.lastRefillNanos).coerceAtLeast(0L)

        val refilledTokens = (
                current.tokens +
                        elapsedNanos.toDouble() / NANOS_PER_SECOND *
                        refillRatePerSecond
                ).coerceAtMost(maxBucketCapacity.toDouble())

        val missingTokens = (1.0 - refilledTokens).coerceAtLeast(0.0)

        val waitNanos =
            missingTokens / refillRatePerSecond * NANOS_PER_SECOND

        return ceil(waitNanos / NANOS_PER_MILLISECOND)
            .toLong()
            .coerceAtLeast(1L)
    }

    private companion object {
        const val NANOS_PER_SECOND = 1_000_000_000.0
        const val NANOS_PER_MILLISECOND = 1_000_000.0
    }
}