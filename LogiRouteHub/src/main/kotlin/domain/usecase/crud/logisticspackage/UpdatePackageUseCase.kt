package domain.usecase.crud.logisticspackage

import domain.model.Package
import domain.model.updateinput.PackageUpdateModel
import domain.repository.PackageRepository

class UpdatePackageUseCase(
    private val packageRepository: PackageRepository
) {
    suspend operator fun invoke(
        id : String ,
        updateRequest: PackageUpdateModel
    )  : Package{
        return packageRepository.update(id , updateRequest)
    }
}
