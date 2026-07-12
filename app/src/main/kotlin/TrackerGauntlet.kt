fun main() {
    println("Problem 1: Priority Dispatcher")
    priorityDispatcher()

    println("\nProblem 2: Waypoints Reverser")
    waypointsReverser()

    println("\nProblem 3: Max Weight Filter")
    findMaxValidWeight()

    println("\nProblem 4: Palindrome Transit Code")
    checkTransitCode()

    println("\nProblem 5: Binary Search Lookup")
    binarySearchLookup()
}

fun priorityDispatcher() {
    for (number in 1..50) {
        if (number % 3 == 0 && number % 5 == 0) {
            println("PriorityExpress")
        } else if (number % 3 == 0) {
            println("Express")
        } else if (number % 5 == 0) {
            println("Freight")
        } else {
            println(number)
        }
    }
}

fun waypointsReverser() {
    val routeWaypoints = arrayOf("HubA", "HubB", "HubC")

    for (waypointIndex in routeWaypoints.size - 1 downTo 0) {
        print(routeWaypoints[waypointIndex])

        if (waypointIndex != 0) {
            print(" -> ")
        }
    }

    println()
}

fun findMaxValidWeight() {
    val packageWeights = doubleArrayOf(10.5, 25.2, 18.9, -1.0, 31.7, 22.4)
    var maxValidWeight = 0.0

    for (packageWeight in packageWeights) {
        if (packageWeight > 0 && packageWeight > maxValidWeight) {
            maxValidWeight = packageWeight
        }
    }

    println("Max valid package weight is: $maxValidWeight")
}

fun checkTransitCode() {
    val transitCode = "TR808RT"
    var reversedTransitCode = ""

    for (codeIndex in transitCode.length - 1 downTo 0) {
        reversedTransitCode += transitCode[codeIndex]
    }

    if (transitCode == reversedTransitCode) {
        println("Palindrome")
    } else {
        println("Not Palindrome")
    }
}

fun binarySearchLookup() {
    val trackingNumbers = intArrayOf(4, 10, 13, 20, 26, 31, 47, 55, 68, 79)
    val targetTrackingNumber = 47

    var leftIndex = 0
    var rightIndex = trackingNumbers.size - 1
    var isTrackingNumberFound = false

    while (leftIndex <= rightIndex) {
        val middleIndex = (leftIndex + rightIndex) / 2
        val middleTrackingNumber = trackingNumbers[middleIndex]

        println("Left index: $leftIndex")
        println("Right index: $rightIndex")
        println("Middle index: $middleIndex")
        println("Middle value: $middleTrackingNumber")
        println("-------------------------")

        if (middleTrackingNumber == targetTrackingNumber) {
            println("Tracking number $targetTrackingNumber found at index $middleIndex")
            isTrackingNumberFound = true
            break
        } else if (middleTrackingNumber < targetTrackingNumber) {
            leftIndex = middleIndex + 1
        } else {
            rightIndex = middleIndex - 1
        }
    }

    if (!isTrackingNumberFound) {
        println("Tracking number $targetTrackingNumber was not found")
    }

    println("Estimated Big O notation: O(log n)")
}