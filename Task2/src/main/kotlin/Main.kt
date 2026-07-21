
import parser.PackageParser
fun main() {

  val packageFilePath = "src/main/resources/packages.csv"
  val packageParser = PackageParser()

  val logisticsPackageRows =
    packageParser.readLogisticsPackageRowsFromFile(packageFilePath)

  logisticsPackageRows.forEach { logisticsPackageRow ->
    println(logisticsPackageRow)
  }
   
}