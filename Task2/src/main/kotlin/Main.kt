import algorithm.PackageSelectionSorter
import parser.PackageParser

fun main() {

  println("=== Package Parser ===")

  val packageFilePath = "src/main/resources/packages.csv"

  val packageParser = PackageParser()
  val packageSelectionSorter = PackageSelectionSorter()

  val logisticsPackageRows =
    packageParser
      .readLogisticsPackageRowsFromFile(packageFilePath)
      .toMutableList()

  packageSelectionSorter
    .sortPackagesByPriorityAndWeight(logisticsPackageRows)

  println()
  println("Successfully parsed packages: ${logisticsPackageRows.size}")
  println()

  println("Sorted packages:")

  logisticsPackageRows.forEach { logisticsPackageRow ->
    println(logisticsPackageRow)
  }

  println()
  println("Package sorting completed.")
}