package domain.tree.simulation

class UnbalancedBST {

    private data class Node(
        val trackingId: String, var left: Node? = null, var right: Node? = null
    )

    private var root: Node? = null

    fun insert(trackingId: String) {
        root = insertNode(root, trackingId)
    }

    private fun insertNode(
        currentNode: Node?, trackingId: String
    ): Node {
        if (currentNode == null) {
            return Node(trackingId)
        }

        if (trackingId < currentNode.trackingId) {
            currentNode.left = insertNode(
                currentNode.left, trackingId
            )
        } else if (trackingId > currentNode.trackingId) {
            currentNode.right = insertNode(
                currentNode.right, trackingId
            )
        }

        return currentNode
    }

    fun searchSteps(trackingId: String): Int {
        var currentNode = root
        var searchSteps = 0

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
