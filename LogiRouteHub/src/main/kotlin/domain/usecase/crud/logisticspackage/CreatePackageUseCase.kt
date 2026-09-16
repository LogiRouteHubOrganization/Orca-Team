package domain.usecase.crud.logisticspackage

import domain.repository.PackageRepository
import domain.model.Package

class CreatePackageUseCase (
    private val packageRepository:PackageRepository
) {
    suspend operator fun  invoke (logisticsPackage : Package): Package {
        return packageRepository.add(logisticsPackage)

    }
}
