package domain.usecase
import domain.model.Vehicle
import domain.model.Package
class ValidateVehicleCapacityUseCase{
    operator fun invoke(vehicle:Vehicle,packages:List<Package>)
    : Boolean {
        return packages.sumOf { it.weightByKg } <= vehicle.maxCapacityKg
    }

}