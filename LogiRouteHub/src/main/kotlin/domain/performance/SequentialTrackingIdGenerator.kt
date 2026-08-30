package domain.performance

class SequentialTrackingIdGenerator {

    fun generatePackageTrackingIds(numberOfTrackingIds: Int): List<String> {
        return (1..numberOfTrackingIds).map { number ->
            "PKG-%06d".format(number)
        }
    }
}
