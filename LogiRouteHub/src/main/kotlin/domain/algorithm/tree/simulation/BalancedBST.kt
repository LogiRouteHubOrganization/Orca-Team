package domain.algorithm.tree.simulation

class BalancedBST {
    private val firstIndex = 0
    private val indexOffset = 1
    private val numberOfHalves = 2
    private val initialSearchSteps = 0

    private data class Node(
        val trackingId: String,
        var left: Node? = null,
        var right: Node? = null
    )

    private var root: Node? = null

    fun build(trackingIds: List<String>) {
        root = buildBalancedTree(
            trackingIds = trackingIds,
            startIndex = firstIndex,
            endIndex = trackingIds.lastIndex
        )
    }

    private fun buildBalancedTree(
        trackingIds: List<String>,
        startIndex: Int,
        endIndex: Int
    ): Node? {
        if (startIndex > endIndex) {
            return null
        }
        val middleIndex = startIndex + (endIndex - startIndex) / numberOfHalves

        return Node(
            trackingId = trackingIds[middleIndex],
            left = buildBalancedTree(
                trackingIds = trackingIds,
                startIndex = startIndex,
                endIndex = middleIndex - indexOffset
            ),
            right = buildBalancedTree(
                trackingIds = trackingIds,
                startIndex = middleIndex + indexOffset,
                endIndex = endIndex
            )
        )
    }

    fun searchSteps(trackingId: String): Int {
        var currentNode = root
        var searchSteps = initialSearchSteps

        while (currentNode != null) {
            searchSteps++

            when {
                trackingId == currentNode.trackingId -> {
                    return searchSteps
                }

                trackingId < currentNode.trackingId -> {
                    currentNode = currentNode.left
                }

                else -> {
                    currentNode = currentNode.right
                }
            }
        }

        return searchSteps
    }
}
