import algorithm.sortPackages
import parser.PackageParser

fun main() {

    val packageFilePath = "src/main/resources/packages.csv"

    val packageParser = PackageParser()

    val logisticsPackageRows =
        packageParser
            .readLogisticsPackageRowsFromFile(packageFilePath)
            .toMutableList()

    sortPackages(logisticsPackageRows)

    println("Successfully parsed packages: ${logisticsPackageRows.size}")
    println()

    println("Top 3 priority packages:")

    val topPackageCount =
        minOf(3, logisticsPackageRows.size)

    for (packageIndex in 0 until topPackageCount) {
        println("${packageIndex + 1}. ${logisticsPackageRows[packageIndex]}")
    }
}