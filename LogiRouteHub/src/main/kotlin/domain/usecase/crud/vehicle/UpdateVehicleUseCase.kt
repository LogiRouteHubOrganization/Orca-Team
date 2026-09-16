package domain.usecase.crud.vehicle

import domain.model.Vehicle
import domain.model.updateinput.VehicleUpdateModel
import domain.repository.VehicleRepository

class UpdateVehicleUseCase(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(
        id: String,
        updateModel: VehicleUpdateModel
    ): Vehicle {
        return vehicleRepository.update(id, updateModel)
    }
}

