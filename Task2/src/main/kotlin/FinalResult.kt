import algorithm.PackageSelectionSorter
import parser.PackageParser

fun main() {

    val packageFilePath = "src/main/resources/packages.csv"

    val packageParser = PackageParser()
    val packageSelectionSorter = PackageSelectionSorter()

    val logisticsPackageRows = packageParser.readLogisticsPackageRowsFromFile(packageFilePath).toMutableList()

    // Sort packages by priority first, then by weight.
    packageSelectionSorter.sortPackagesByPriorityAndWeight(logisticsPackageRows)

    println("Successfully parsed packages: ${logisticsPackageRows.size}")
    println()

    println("Top 3 priority packages:")

    for (packageIndex in 0 until 3) {
        println("${packageIndex + 1}. ${logisticsPackageRows[packageIndex]}")
    }
}