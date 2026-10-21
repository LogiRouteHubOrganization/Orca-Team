package data.remote.ratelimit



class RateLimiter(
    private val maxBucketCapacity: Int,
    private val refillRatePerSecond: Double,
    private val nanoTimeSource: NanoTimeSource = SystemNanoTimeSource
) {
    private var tokens = maxBucketCapacity.toDouble()

    init {
        require(maxBucketCapacity > 0)
        require(refillRatePerSecond.isFinite() && refillRatePerSecond > 0.0)
    }

    fun tryAcquire(): Boolean {
        if (tokens < 1.0) return false

        tokens -= 1.0
        return true
    }
}