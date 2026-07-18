
import parser.PackageParser

fun main() {
  val filePath = "src/main/resources/packages.csv"
  val packageParser = PackageParser()

  val packages = packageParser.parse(filePath)

  packages.forEach { logisticsPackage ->
    println(logisticsPackage)
  }
}