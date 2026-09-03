package domain.model.request

data class PackagePriorityLevels(
    val urgent: Int = 3,
    val standard: Int = 2,
    val low: Int = 1,
    val unknown: Int = 0
)
