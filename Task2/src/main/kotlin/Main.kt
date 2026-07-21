
import parser.PackageParser

fun main() {

  println("=== Package Parser ===")

  val packageFilePath = "src/main/resources/packages.csv"

  val packageParser = PackageParser()

  val logisticsPackageRows =
    packageParser.readLogisticsPackageRowsFromFile(packageFilePath)

  println()
  println("Successfully parsed packages: ${logisticsPackageRows.size}")
  println()

  logisticsPackageRows.forEach { logisticsPackageRow ->
    println(logisticsPackageRow)
  }

  println()
  println("Package parsing completed.")
}