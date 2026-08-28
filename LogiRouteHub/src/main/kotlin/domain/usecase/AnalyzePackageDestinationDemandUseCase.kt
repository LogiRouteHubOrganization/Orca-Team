package domain.usecase
import domain.model.Warehouse
import domain.model.Package
class AnalyzePackageDestinationDemandUseCase {
    operator fun invoke(packages: List<Package>): Warehouse? {

        return packages.groupingBy { it.destinationWarehouse }
            .eachCount().maxByOrNull { it.value } ?.key
    }
}


