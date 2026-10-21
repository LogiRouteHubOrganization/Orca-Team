package data.remote.ratelimit


class RateLimiter(
    private val maxBucketCapacity: Int,
    private val refillRatePerSecond: Double,
    private val nanoTimeSource: NanoTimeSource = SystemNanoTimeSource
) {
    private var tokens = maxBucketCapacity.toDouble()
    private var lastRefillNanos = nanoTimeSource.nanoTime()

    init {
        require(maxBucketCapacity > 0)
        require(refillRatePerSecond.isFinite() && refillRatePerSecond > 0.0)
    }

    fun tryAcquire(): Boolean {
        refillTokens()

        if (tokens < 1.0) return false

        tokens -= 1.0
        return true
    }

    private fun refillTokens() {
        val now = nanoTimeSource.nanoTime()
        val elapsedNanos = (now - lastRefillNanos).coerceAtLeast(0L)

        val tokensToAdd =
            elapsedNanos.toDouble() / NANOS_PER_SECOND * refillRatePerSecond

        tokens = (tokens + tokensToAdd).coerceAtMost(maxBucketCapacity.toDouble())
        lastRefillNanos = now
    }

    private companion object {
        const val NANOS_PER_SECOND = 1_000_000_000.0
    }
}