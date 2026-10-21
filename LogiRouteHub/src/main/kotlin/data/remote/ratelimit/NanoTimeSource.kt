package data.remote.ratelimit

fun interface NanoTimeSource {
    fun nanoTime(): Long
}

object SystemNanoTimeSource : NanoTimeSource {
    override fun nanoTime(): Long = System.nanoTime()
}