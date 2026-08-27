package domain.usecase
import domain.model.Warehouse
import domain.model.Vehicle
class AddVehicleToHubUseCase {
    operator fun invoke(

        warehouse: Warehouse,
        vehicle: Vehicle): Warehouse {
        warehouse.addVehicle(vehicle)
        vehicle.currentWarehouse = warehouse
        return warehouse
    }
}